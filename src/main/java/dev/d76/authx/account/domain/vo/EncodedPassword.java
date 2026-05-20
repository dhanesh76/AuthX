package dev.d76.authx.account.domain.vo;

import lombok.NonNull;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public record EncodedPassword(String value) {

    public EncodedPassword {
        Objects.requireNonNull(value, "Encoded password must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Encoded password must not be blank");
        }
    }

    @Override
    public @NonNull String toString() {
        return "EncodedPassword[REDACTED]";
    }
}