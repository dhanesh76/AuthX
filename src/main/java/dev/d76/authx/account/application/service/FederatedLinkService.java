package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.model.dto.LoginResponse;
import dev.d76.authx.account.application.model.flow.FlowIntent;
import dev.d76.authx.account.application.model.token.IssuedToken;
import dev.d76.authx.account.application.port.in.FederatedLinkUseCase;
import dev.d76.authx.account.application.port.out.RefreshTokenPort;
import dev.d76.authx.account.application.port.out.TokenPort;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.model.IdentityProvider;
import dev.d76.authx.account.domain.model.Role;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FederatedLinkService implements FederatedLinkUseCase {

    private final TokenPort tokenPort;
    private final AccountRepository accountRepository;
    private final RefreshTokenPort refreshTokenPort;

    @Override
    @Transactional
    public LoginResponse execute(String flowToken) {

        TokenPort.FlowClaims claims = tokenPort.verifyFlowToken(flowToken, FlowIntent.LINK_IDENTITY);

        Email email = new Email(claims.email());
        IdentityProvider provider = IdentityProvider.valueOf(claims.identityProvider());

        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));

        // linkProvider enforces: not blocked, not already linked, auto-activates VERIFICATION_PENDING
        account.linkProvider(provider);
        accountRepository.save(account);

        Account.AccountSnapshot snap = account.snapshot();

        IssuedToken accessToken = tokenPort.issueAccessToken(new TokenPort.AccessTokenRequest(
                snap.accountId().toString(),
                snap.email(),
                snap.roles().stream().map(Role::toAuthority).toList()
        ));

        return new LoginResponse(
                snap.accountId().toString(),
                snap.email(),
                accessToken.value(),
                refreshTokenPort.issue(snap.accountId().toString())
        );
    }
}