package dev.d76.authx.challenge.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record ChallengeId(UUID value) {

    public ChallengeId {
        Objects.requireNonNull(value, "ChallengeId must not be null");
    }

    public ChallengeId() {
        this(UUID.randomUUID());
    }
}