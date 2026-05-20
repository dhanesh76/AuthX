package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.model.dto.LoginResponse;
import dev.d76.authx.account.application.model.flow.FlowIntent;
import dev.d76.authx.account.application.model.token.IssuedToken;
import dev.d76.authx.account.application.port.in.FederatedRegisterUseCase;
import dev.d76.authx.account.application.port.out.RefreshTokenPort;
import dev.d76.authx.account.application.port.out.TokenPort;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.model.IdentityProvider;
import dev.d76.authx.account.domain.model.Role;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.account.domain.vo.Username;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FederatedRegisterService implements FederatedRegisterUseCase {

    private final TokenPort tokenPort;
    private final AccountRepository accountRepository;
    private final RefreshTokenPort refreshTokenPort;

    @Override
    @Transactional
    public LoginResponse execute(String flowToken, Username username) {

        TokenPort.FlowClaims claims = tokenPort.verifyFlowToken(flowToken, FlowIntent.OPEN_ACCOUNT);

        Email email = new Email(claims.email());
        IdentityProvider provider = IdentityProvider.valueOf(claims.identityProvider());

        // Race condition guard — username may have been taken since the flow token was issued
        if (accountRepository.existsByUsername(username)) {
            throw new BusinessException(AccountErrorCode.USERNAME_TAKEN);
        }

        Account account = Account.createFederateAccount(username, email, provider, Instant.now());
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