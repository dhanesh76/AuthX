package dev.d76.authx.account.infrastructure.security.token.refresh;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_account_id", columnList = "account_id")
})
public class JpaRefreshToken {

    @Id
    @Column(name = "token_hash", nullable = false, updatable = false, length = 64)
    private String tokenHash;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected JpaRefreshToken() {
    }

    public JpaRefreshToken(String tokenHash, UUID accountId,
                           Instant issuedAt, Instant expiresAt, Instant revokedAt) {
        this.tokenHash = tokenHash;
        this.accountId = accountId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public UUID accountId() {
        return accountId;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }
}