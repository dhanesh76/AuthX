package dev.d76.authx.account.infrastructure.security.token.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface SpringDataRefreshTokenRepository
        extends JpaRepository<JpaRefreshToken, String> {

    @Modifying
    @Query("DELETE FROM JpaRefreshToken t WHERE t.accountId = :accountId")
    void deleteAllByAccountId(UUID accountId);

    @Modifying
    @Query("DELETE FROM JpaRefreshToken t WHERE t.expiresAt < :now OR t.revokedAt IS NOT NULL")
    void deleteExpiredAndRevoked(Instant now);
}