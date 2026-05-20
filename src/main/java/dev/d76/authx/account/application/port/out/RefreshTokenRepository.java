package dev.d76.authx.account.application.port.out;

import dev.d76.authx.account.application.model.token.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    void save(RefreshToken snapshot);

    Optional<RefreshToken> findByHash(String tokenHash);

    void deleteByHash(String tokenHash);

    void deleteAllByAccountId(UUID accountId);

    void deleteExpiredAndRevoked(Instant now);
}