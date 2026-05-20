package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.model.dto.LoginResponse;
import dev.d76.authx.account.application.model.token.IssuedToken;
import dev.d76.authx.account.application.port.in.LoginUseCase;
import dev.d76.authx.account.application.port.out.RefreshTokenPort;
import dev.d76.authx.account.application.port.out.TokenPort;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.model.Role;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.account.domain.port.out.PasswordEncoder;
import dev.d76.authx.account.domain.vo.RawPassword;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenPort tokenPort;
    private final RefreshTokenPort refreshTokenPort;

    @Override
    @Transactional
    public LoginResponse execute(String usernameOrEmail, RawPassword password) {

        Account account = accountRepository
                .findByEmailOrUsername(usernameOrEmail)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.INVALID_CREDENTIALS));

        account.verifyPassword(password.value(), passwordEncoder);

        Account.AccountSnapshot snap = account.snapshot();

        IssuedToken accessToken = tokenPort.issueAccessToken(new TokenPort.AccessTokenRequest(
                snap.accountId().toString(),
                snap.email(),
                snap.roles().stream().map(Role::toAuthority).toList()
        ));

        String refreshToken = refreshTokenPort.issue(snap.accountId().toString());

        return new LoginResponse(
                snap.accountId().toString(),
                snap.email(),
                accessToken.value(),
                refreshToken
        );
    }
}