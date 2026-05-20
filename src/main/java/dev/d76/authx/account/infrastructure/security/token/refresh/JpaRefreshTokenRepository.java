package dev.d76.authx.account.infrastructure.security.token.refresh;

import dev.d76.authx.account.application.model.token.RefreshToken;
import dev.d76.authx.account.application.port.out.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository store;

    @Override
    public void save(RefreshToken snapshot) {
        store.save(toEntity(snapshot));
    }

    @Override
    public Optional<RefreshToken> findByHash(String tokenHash) {
        return store.findById(tokenHash).map(this::toSnapshot);
    }

    @Override
    public void deleteByHash(String tokenHash) {
        store.deleteById(tokenHash);
    }

    @Override
    public void deleteAllByAccountId(UUID accountId) {
        store.deleteAllByAccountId(accountId);
    }

    @Override
    public void deleteExpiredAndRevoked(Instant now) {
        store.deleteExpiredAndRevoked(now);
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private JpaRefreshToken toEntity(RefreshToken snap) {
        return new JpaRefreshToken(
                snap.tokenHash(),
                snap.accountId(),
                snap.issuedAt(),
                snap.expiresAt(),
                snap.revokedAt()
        );
    }

    private RefreshToken toSnapshot(JpaRefreshToken entity) {
        return new RefreshToken(
                entity.tokenHash(),
                entity.accountId(),
                entity.issuedAt(),
                entity.expiresAt(),
                entity.revokedAt()
        );
    }
}