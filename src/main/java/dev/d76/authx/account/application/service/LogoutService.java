package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.port.in.LogoutUseCase;
import dev.d76.authx.account.application.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenPort refreshTokenPort;

    @Override
    @Transactional
    public void execute(String rawRefreshToken) {
        refreshTokenPort.revoke(rawRefreshToken);
    }
}