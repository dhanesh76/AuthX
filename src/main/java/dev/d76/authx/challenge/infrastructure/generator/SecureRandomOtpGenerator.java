package dev.d76.authx.challenge.infrastructure.generator;

import dev.d76.authx.challenge.domain.model.ChallengeType;
import dev.d76.authx.challenge.domain.port.out.ChallengeSecretGenerator;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomOtpGenerator implements ChallengeSecretGenerator {
    private static final int BOUND;
    private static final SecureRandom random;

    static {
        BOUND = (int) Math.pow(10, 6);
        random = new SecureRandom();
    }

    @Override
    public boolean supports(ChallengeType challengeType) {
        return ChallengeType.OTP.equals(challengeType);
    }

    @Override
    public ChallengeSecret generate() {
        int randomNumber = random.nextInt(BOUND);
        String code = String.format("%06d", randomNumber);
        return new ChallengeSecret(code);
    }
}
