package dev.d76.authx.challenge.infrastructure.persistence;

import dev.d76.authx.challenge.domain.model.Challenge;
import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.port.out.ChallengeRepository;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.authx.platform.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CacheChallengeRepository implements ChallengeRepository {

    private final CacheService cacheService;

    @Override
    public void save(Challenge challenge) {
        var snapshot = challenge.snapshot();

        long ttlSeconds = Math.max(
                0,
                Duration.between(Instant.now(), snapshot.expiresAt()).getSeconds()
        );

        cacheService.put(
                constructKey(snapshot.email(), snapshot.purpose()),
                snapshot,
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public Optional<Challenge> findLatestPending(Email email, ChallengePurpose purpose) {
        return cacheService
                .get(constructKey(email, purpose), Challenge.ChallengeSnapshot.class)
                .map(Challenge::from);
    }

    @Override
    public void deleteAll(Email email, ChallengePurpose purpose) {
        cacheService.evict(constructKey(email, purpose));
    }

    /**
     * Key encodes usernameOrEmail + purpose so that REGISTRATION and PASSWORD_RESET challenges
     * for the same usernameOrEmail coexist independently. The "challenge:" prefix namespaces
     * keys to avoid collision with other bounded contexts sharing the same Redis instance.
     */
    private String constructKey(Email email, ChallengePurpose purpose) {
        return "challenge:" + email.value() + ":" + purpose.name();
    }
}