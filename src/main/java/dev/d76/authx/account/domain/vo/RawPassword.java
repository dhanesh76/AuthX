package dev.d76.authx.account.domain.vo;

import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.spring.exception.BusinessException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.regex.Pattern;

@ValueObject
public record RawPassword(String value) {

    private static final Pattern FORMAT = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
    );

    public RawPassword {
        Objects.requireNonNull(value, "Password must not be null");

        if (!FORMAT.matcher(value).matches()) {
            throw new BusinessException(AccountErrorCode.INVALID_PASSWORD);
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o; // never compare raw passwords structurally
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public @NonNull String toString() {
        return "RawPassword[REDACTED]";
    }
}