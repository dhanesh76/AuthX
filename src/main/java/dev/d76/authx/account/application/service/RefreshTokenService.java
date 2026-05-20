package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.model.token.RefreshToken;
import dev.d76.authx.account.application.port.out.RefreshTokenPort;
import dev.d76.authx.account.application.port.out.RefreshTokenRepository;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenPort {

    private static final Duration TTL = Duration.ofDays(7);
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    // ── RefreshTokenPort implementation ──────────────────────────────────────

    @Override
    @Transactional
    public String issue(String accountId) {
        refreshTokenRepository.deleteAllByAccountId(UUID.fromString(accountId));
        return persist(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public String verify(String rawToken) {
        return findActiveOrThrow(rawToken).accountId().toString();
    }

    @Override
    @Transactional
    public String rotate(String rawToken, String accountId) {
        RefreshToken current = findActiveOrThrow(rawToken);
        refreshTokenRepository.deleteByHash(current.tokenHash());
        return persist(accountId);
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        String tokenHash = hash(rawToken);
        refreshTokenRepository.findByHash(tokenHash).ifPresent(snap ->
                refreshTokenRepository.save(new RefreshToken(
                        snap.tokenHash(),
                        snap.accountId(),
                        snap.issuedAt(),
                        snap.expiresAt(),
                        Instant.now()
                ))
        );
        // silent no-op if not found — logout must never fail
    }

    @Override
    @Transactional
    public void revokeAll(String accountId) {
        refreshTokenRepository.deleteAllByAccountId(UUID.fromString(accountId));
    }

    // ── Internal — RefreshToken model only appears here ──────────────────────

    private String persist(String accountId) {
        String raw = generateRaw();
        Instant now = Instant.now();
        refreshTokenRepository.save(new RefreshToken(
                hash(raw),
                UUID.fromString(accountId),
                now,
                now.plus(TTL),
                null
        ));
        return raw;
    }

    private RefreshToken findActiveOrThrow(String rawToken) {
        String tokenHash = hash(rawToken);
        Instant now = Instant.now();

        RefreshToken token = refreshTokenRepository
                .findByHash(tokenHash)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.INVALID_TOKEN));

        if (!token.isActive(now)) {
            log.warn("Inactive refresh token presented — hash={}", tokenHash);
            refreshTokenRepository.deleteByHash(tokenHash);
            throw new BusinessException(AccountErrorCode.INVALID_TOKEN);
        }

        return token;
    }

    private String generateRaw() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(raw.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}