package dev.d76.authx.challenge.domain.vo;

import lombok.NonNull;

import java.util.Objects;

public record ChallengeSecret(String value) {

    public ChallengeSecret {
        Objects.requireNonNull(value, "Challenge secret must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Challenge secret must not be blank");
        }
    }

    @Override @NonNull
    public String toString() {
        return "ChallengeSecret[REDACTED]";
    }
}