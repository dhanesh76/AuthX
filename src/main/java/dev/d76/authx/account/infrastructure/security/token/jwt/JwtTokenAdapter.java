package dev.d76.authx.account.infrastructure.security.token.jwt;

import dev.d76.authx.account.application.model.flow.FlowIntent;
import dev.d76.authx.account.application.model.flow.ReAuthIntent;
import dev.d76.authx.account.application.model.token.IssuedToken;
import dev.d76.authx.account.application.port.out.TokenPort;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.infrastructure.security.config.JwtProperties;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenPort {

    private static final String ISSUER = "d76-identity";

    private final JwtProperties props;

    // ── Core ─────────────────────────────────────────────────────────────────

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(props.secret().getBytes());
    }

    private IssuedToken buildToken(String subject, Duration ttl, Map<String, Object> claims) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(ttl);

        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject(subject)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claims(claims)
                .signWith(secretKey(), Jwts.SIG.HS256)
                .compact();

        return new IssuedToken(token, issuedAt, expiresAt);
    }

    private Claims parse(String raw) {
        try {
            return Jwts.parser()
                    .requireIssuer(ISSUER)
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(raw)
                    .getPayload();
        } catch (JwtException ex) {
            log.warn("JWT verification failed: {}", ex.getMessage());
            throw new BusinessException(AccountErrorCode.INVALID_TOKEN,
                    "Invalid or expired token");
        }
    }

    // ── Guards ───────────────────────────────────────────────────────────────

    /**
     * Verifies the {@code typ} claim matches the expected token type.
     * This check runs first on every parse to prevent token substitution attacks —
     * a flow token must not be accepted where an access token is expected.
     */
    private void assertTokenType(Claims claims, TokenType expected) {
        TokenType actual = parseEnum(
                claims,
                IdentityAttributes.TOKEN_TYPE,
                TokenType.class,
                claims.getSubject(),
                expected.name()
        );

        if (actual != expected) {
            log.warn("Token type substitution — expected={} actual={} subject={}",
                    expected, actual, claims.getSubject());
            reject("token type mismatch", claims.getSubject(), expected.name());
        }
    }

    private String require(Claims claims, String key) {
        String value = claims.get(key, String.class);
        if (value == null || value.isBlank()) {
            reject("required claim missing: " + key, claims.getSubject(), "n/a");
        }
        return value;
    }

    private <E extends Enum<E>> E parseEnum(
            Claims claims,
            String key,
            Class<E> type,
            String subject,
            String context) {
        String raw = claims.get(key, String.class);
        try {
            return Enum.valueOf(type, raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            reject("unrecognised value for claim '" + key + "'", subject, context);
            throw new IllegalStateException("unreachable");
        }
    }

    private void reject(String reason, String subject, String context) {
        log.warn("Token rejected — reason='{}' subject='{}' context='{}'",
                reason, subject, context);
        throw new BusinessException(AccountErrorCode.INVALID_TOKEN);
    }

    // ── Issue ────────────────────────────────────────────────────────────────

    @Override
    public IssuedToken issueAccessToken(AccessTokenRequest req) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(IdentityAttributes.TOKEN_TYPE, TokenType.ACCESS.name());
        claims.put(IdentityAttributes.EMAIL, req.email());
        claims.put(IdentityAttributes.ROLES, req.roles());

        return buildToken(req.accountId(), props.accessTtl(), claims);
    }

    @Override
    public IssuedToken issueFlowToken(FlowTokenRequest req) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(IdentityAttributes.TOKEN_TYPE, TokenType.FLOW.name());
        claims.put(IdentityAttributes.FLOW_INTENT, req.intent().name());
        claims.put(IdentityAttributes.IDENTITY_PROVIDER, req.identityProvider());

        return buildToken(req.email().value(), props.flowTtl(), claims);
    }

    @Override
    public IssuedToken issueReAuthToken(ReAuthTokenRequest req) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(IdentityAttributes.TOKEN_TYPE, TokenType.RE_AUTH.name());
        claims.put(IdentityAttributes.RE_AUTH_INTENT, req.intent().name());

        return buildToken(req.email().value(), props.reAuthTtl(), claims);
    }

    // ── Verify ───────────────────────────────────────────────────────────────

    @Override
    public AccessClaims verifyAccessToken(String raw) {
        Claims claims = parse(raw);
        assertTokenType(claims, TokenType.ACCESS);

        List<String> roles = claims.get(IdentityAttributes.ROLES) instanceof List<?> list
                ? list.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList()
                : List.of();

        return new AccessClaims(
                claims.getSubject(),
                require(claims, IdentityAttributes.EMAIL),
                roles
        );
    }

    @Override
    public FlowClaims verifyFlowToken(String raw, FlowIntent expectedIntent) {
        Claims claims = parse(raw);
        assertTokenType(claims, TokenType.FLOW);

        FlowIntent actual = parseEnum(
                claims,
                IdentityAttributes.FLOW_INTENT,
                FlowIntent.class,
                claims.getSubject(),
                expectedIntent.name()
        );

        if (actual != expectedIntent) {
            reject("flow intent mismatch", claims.getSubject(), expectedIntent.name());
        }

        return new FlowClaims(
                claims.getSubject(),
                actual,
                require(claims, IdentityAttributes.IDENTITY_PROVIDER)
        );
    }

    @Override
    public ReAuthClaims verifyReAuthToken(
            String raw,
            Email expectedSubject,
            ReAuthIntent expectedIntent) {
        Claims claims = parse(raw);
        assertTokenType(claims, TokenType.RE_AUTH);

        if (!expectedSubject.value().equals(claims.getSubject())) {
            reject("subject mismatch", claims.getSubject(), expectedIntent.name());
        }

        ReAuthIntent actual = parseEnum(
                claims,
                IdentityAttributes.RE_AUTH_INTENT,
                ReAuthIntent.class,
                claims.getSubject(),
                expectedIntent.name()
        );

        if (actual != expectedIntent) {
            reject("re-auth intent mismatch", claims.getSubject(), expectedIntent.name());
        }

        return new ReAuthClaims(claims.getSubject(), actual);
    }
}