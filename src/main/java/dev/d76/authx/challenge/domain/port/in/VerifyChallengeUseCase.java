package dev.d76.authx.challenge.domain.port.in;

import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.model.ChallengeType;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import dev.d76.authx.kernel.vo.Email;

public interface VerifyChallengeUseCase {
    void execute(Email email, ChallengeSecret attempted, ChallengePurpose issuedFor, ChallengeType type);
}
