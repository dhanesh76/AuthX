package dev.d76.authx.challenge.domain.model;

import dev.d76.authx.challenge.domain.exception.ChallengeErrorCode;
import dev.d76.authx.challenge.domain.vo.ChallengeId;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.time.Duration;
import java.time.Instant;

@AggregateRoot
public class Challenge {

    private ChallengeId challengeId;
    private Email email;
    private ChallengeSecret challengeSecret;
    private ChallengePurpose challengePurpose;
    private ChallengeType challengeType;
    private Instant issuedAt;
    private Instant expiresAt;
    private ChallengeStatus challengeStatus;

    private Challenge(
            ChallengeId challengeId,
            Email email,
            ChallengeSecret secret,
            ChallengePurpose purpose,
            ChallengeType type,
            Instant issuedAt,
            Instant expiresAt
    ) {
        this.challengeId = challengeId;
        this.email = email;
        this.challengeSecret = secret;
        this.challengePurpose = purpose;
        this.challengeType = type;
        this.challengeStatus = ChallengeStatus.PENDING;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public static Challenge issue(
            Email email,
            ChallengeSecret secret,
            ChallengePurpose purpose,
            ChallengeType type,
            Instant now,
            Duration ttl
    ) {
        return new Challenge(new ChallengeId(), email, secret, purpose, type, now, now.plus(ttl));
    }

    // ── Snapshot ─────────────────────────────────────────────────────────────

    public record ChallengeSnapshot(
            ChallengeId challengeId,
            Email email,
            ChallengeSecret secret,
            ChallengePurpose purpose,
            ChallengeType type,
            Instant issuedAt,
            Instant expiresAt,
            ChallengeStatus status
    ) {}

    public ChallengeSnapshot snapshot() {
        return new ChallengeSnapshot(
                challengeId,
                email,
                challengeSecret,
                challengePurpose,
                challengeType,
                issuedAt,
                expiresAt,
                challengeStatus
        );
    }

    public static Challenge from(ChallengeSnapshot snapshot) {
        Challenge challenge = new Challenge(
                snapshot.challengeId(),
                snapshot.email(),
                snapshot.secret(),
                snapshot.purpose(),
                snapshot.type(),
                snapshot.issuedAt(),
                snapshot.expiresAt()
        );
        challenge.challengeStatus = snapshot.status();
        return challenge;
    }

    // ── Behaviour ────────────────────────────────────────────────────────────

    public void verify(ChallengeSecret attempted, ChallengePurpose issuedFor, Instant now) {
        requirePending();
        requireNotExpired(now);

        if (!challengePurpose.equals(issuedFor) || !challengeSecret.equals(attempted)) {
            throw new BusinessException(ChallengeErrorCode.INVALID_CHALLENGE_SECRET);
        }
        this.challengeStatus = ChallengeStatus.CONSUMED;
    }

    private void requirePending() {
        if (!ChallengeStatus.PENDING.equals(challengeStatus)) {
            throw new BusinessException(ChallengeErrorCode.CHALLENGE_NOT_PENDING);
        }
    }

    private void requireNotExpired(Instant now) {
        if (now.isAfter(expiresAt)) {
            this.challengeStatus = ChallengeStatus.EXPIRED;
            throw new BusinessException(ChallengeErrorCode.CHALLENGE_EXPIRED);
        }
    }
}