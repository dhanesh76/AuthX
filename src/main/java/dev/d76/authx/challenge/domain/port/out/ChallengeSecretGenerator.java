package dev.d76.authx.challenge.domain.port.out;

import dev.d76.authx.challenge.domain.model.ChallengeType;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;

public interface ChallengeSecretGenerator {
    boolean supports(ChallengeType challengeType);
    ChallengeSecret generate();
}
