package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.model.dto.LoginResponse;
import dev.d76.authx.account.application.model.token.IssuedToken;
import dev.d76.authx.account.application.port.in.RefreshUseCase;
import dev.d76.authx.account.application.port.out.RefreshTokenPort;
import dev.d76.authx.account.application.port.out.TokenPort;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.model.Role;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.account.domain.vo.AccountId;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshService implements RefreshUseCase {

    private final RefreshTokenPort refreshTokenPort;
    private final AccountRepository accountRepository;
    private final TokenPort tokenPort;

    @Override
    @Transactional
    public LoginResponse execute(String rawRefreshToken) {

        String accountId = refreshTokenPort.verify(rawRefreshToken);

        // Account is reloaded on every refresh so that role changes
        // take effect without requiring a new login
        Account.AccountSnapshot snap = accountRepository
                .findById(new AccountId(UUID.fromString(accountId)))
                .map(Account::snapshot)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));

        IssuedToken accessToken = tokenPort.issueAccessToken(new TokenPort.AccessTokenRequest(
                snap.accountId().toString(),
                snap.email(),
                snap.roles().stream().map(Role::toAuthority).toList()
        ));

        // Rotation: old token is deleted and a new one issued atomically.
        // Presenting the same token twice returns INVALID_TOKEN on the second
        // attempt, which detects stolen token reuse.
        String newRefreshToken = refreshTokenPort.rotate(rawRefreshToken, accountId);

        return new LoginResponse(
                snap.accountId().toString(),
                snap.email(),
                accessToken.value(),
                newRefreshToken
        );
    }
}