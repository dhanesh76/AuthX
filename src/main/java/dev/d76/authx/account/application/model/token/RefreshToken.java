package dev.d76.authx.account.application.model.token;

import java.time.Instant;
import java.util.UUID;

public record RefreshToken(
        String tokenHash,
        UUID accountId,
        Instant issuedAt,
        Instant expiresAt,
        Instant revokedAt
) {
    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}