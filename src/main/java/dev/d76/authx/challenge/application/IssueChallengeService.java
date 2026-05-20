package dev.d76.authx.challenge.application;

import dev.d76.authx.challenge.domain.model.Challenge;
import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.model.ChallengeType;
import dev.d76.authx.challenge.domain.model.DeliveryChannel;
import dev.d76.authx.challenge.domain.port.in.IssueChallengeUseCase;
import dev.d76.authx.challenge.domain.port.out.ChallengeRepository;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import dev.d76.authx.challenge.infrastructure.generator.ChallengeSecretGeneratorRegistry;
import dev.d76.authx.challenge.infrastructure.sender.ChallengeSenderRegistry;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.authx.platform.ratelimit.RateLimitPolicy;
import dev.d76.authx.platform.ratelimit.annotation.RateLimit;
import dev.d76.authx.platform.ratelimit.annotation.RateLimitKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class IssueChallengeService implements IssueChallengeUseCase {

    @Value("${application.otp.ttl}")
    Duration ttl;

    private final ChallengeRepository repository;
    private final ChallengeSenderRegistry senderRegistry;
    private final ChallengeSecretGeneratorRegistry secretGeneratorRegistry;

    @Override
    @RateLimit(policy = RateLimitPolicy.OTP_BY_EMAIL)
    public void execute(@RateLimitKey Email email, ChallengeType type, ChallengePurpose purpose) {
        repository.deleteAll(email, purpose);

        ChallengeSecret secret = secretGeneratorRegistry
                .resolve(type)
                .generate();

        Challenge challenge = Challenge.issue(
                email, secret, purpose, type,
                Instant.now(), ttl
        );

        repository.save(challenge);

        senderRegistry
                .resolve(DeliveryChannel.EMAIL)
                .send(email, secret, purpose);
    }
}