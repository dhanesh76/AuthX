package dev.d76.authx.challenge.domain.port.out;


import dev.d76.authx.challenge.domain.model.Challenge;
import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.kernel.vo.Email;

import java.util.Optional;

public interface ChallengeRepository {

    void save(Challenge challenge);

    /**
     * Returns the most recently issued PENDING challenge for the given usernameOrEmail and purpose.
     * Returns empty if no challenge exists — either never issued, already consumed, or expired.
     */
    Optional<Challenge> findLatestPending(Email email, ChallengePurpose purpose);

    /**
     * Deletes all challenges for the given usernameOrEmail and purpose.
     * Called after successful verification to prevent reuse of old pending challenges,
     * and before issuing a new challenge to ensure only one active challenge exists at a time.
     */
    void deleteAll(Email email, ChallengePurpose purpose);
}