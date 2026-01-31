package d76.app.security.jwt;

import d76.app.auth.exception.AuthErrorCode;
import d76.app.core.exception.BusinessException;
import d76.app.security.jwt.model.JwtPurpose;
import d76.app.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private final String secret;
    private final long accessTokenTTLSeconds;
    private final long actionTokenTTLSeconds;
    private final long reAuthTokenTTLSeconds;

    JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access.tokenTTL}") long accessTokenTTLSeconds,
            @Value("${jwt.action.tokenTTL}") long actionTokenTTLSeconds,
            @Value("${jwt.reAuth.tokenTTL}") long reAuthTokenTTLSeconds
    ) {
        this.secret = secret;
        this.accessTokenTTLSeconds = accessTokenTTLSeconds;
        this.actionTokenTTLSeconds = actionTokenTTLSeconds;
        this.reAuthTokenTTLSeconds = reAuthTokenTTLSeconds;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * ACCESS TOKEN
     */
    public String generateAccessToken(UserPrincipal principal) {

        String jti = UUID.randomUUID().toString();

        List<String> role = principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .id(jti)
                .subject(String.valueOf(principal.getUserId()))
                .claim("email", principal.getUsername())
                .claim("identityProvider", principal.getIdentityProvider().name())
                .claim("roles", role)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(accessTokenTTLSeconds)))
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * ACTION TOKEN
     */

    public String generateActionToken(String email, JwtPurpose purpose, String authProvider) {

        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(email)
                .claim("purpose", purpose.name())
                .claim("identityProvider", authProvider)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(actionTokenTTLSeconds))) // 5 mins
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }

    public void assertActionTokenValid(String token, JwtPurpose expectedPurpose) {

        Claims claims = extractClaims(token);

        boolean purposeMatches = expectedPurpose.name().equals(claims.get("purpose", String.class));
        boolean notExpired = new Date().before(claims.getExpiration());

        if (!purposeMatches || !notExpired) {

            String reason = !purposeMatches ? "purpose_mismatch" : "expired";
            log.warn("Action token rejected for user={} reason={} expectedPurpose={}",
                    claims.getSubject(),
                    reason,
                    expectedPurpose
            );
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * RE_AUTH TOKEN
     */
    public String generateReAuthToken(String email, JwtPurpose jwtPurpose) {
        return Jwts
                .builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("purpose", jwtPurpose.name())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(reAuthTokenTTLSeconds))) // 3 mins
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }

    public void assertReAuthTokenValid(String email, String token, JwtPurpose expectedPurpose) {

        Claims claims = extractClaims(token);

        boolean subjectMatches = email.equals(claims.getSubject());
        boolean purposeMatches = expectedPurpose.name().equals(claims.get("purpose", String.class));
        boolean notExpired = new Date().before(claims.getExpiration());

        if (!purposeMatches || !notExpired || !subjectMatches) {

            String reason = !purposeMatches ? "purpose_mismatch" :
                    !subjectMatches ? "subject_mismatch" : "purpose_mismatch";

            log.warn("ReAuth token rejected for user={} reason={} expected_user={} expectedPurpose={}",
                    claims.getSubject(),
                    reason,
                    email,
                    expectedPurpose
            );
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * CORE
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException ex) {
            log.warn("JWT parsing/verification failed: {}", ex.getMessage(), ex);
            throw new BusinessException(
                    AuthErrorCode.INVALID_TOKEN,
                    "Invalid or tampered token"
            );
        }
    }
}
