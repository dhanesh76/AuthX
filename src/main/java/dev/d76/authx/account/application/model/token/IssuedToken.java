package dev.d76.authx.account.application.model.token;

import java.time.Instant;
import java.util.Objects;

public record IssuedToken(
        String value,
        Instant issuedAt,
        Instant expiresAt
) {
    public IssuedToken {
        Objects.requireNonNull(value,     "Token value must not be null");
        Objects.requireNonNull(issuedAt,  "IssuedAt must not be null");
        Objects.requireNonNull(expiresAt, "ExpiresAt must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("Token value must not be blank");
        }

        if (issuedAt.isAfter(expiresAt)) {
            throw new IllegalStateException("IssuedAt cannot be after ExpiresAt");
        }
    }
}