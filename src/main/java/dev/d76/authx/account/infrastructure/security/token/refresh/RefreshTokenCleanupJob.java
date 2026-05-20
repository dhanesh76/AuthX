package dev.d76.authx.account.infrastructure.security.token.refresh;

import dev.d76.authx.account.application.port.out.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void purge() {
        refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        log.info("Refresh token purge completed at {}", Instant.now());
    }
}