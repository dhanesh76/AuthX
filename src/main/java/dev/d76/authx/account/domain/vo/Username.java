package dev.d76.authx.account.domain.vo;

import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.spring.exception.BusinessException;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

@ValueObject
public record Username(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final int MIN_LEN = 3;
    private static final int MAX_LEN = 30;

    public Username {
        Objects.requireNonNull(value, "Username must not be null");

        String trimmed = value.strip().toLowerCase();

        if (trimmed.length() < MIN_LEN || trimmed.length() > MAX_LEN) {
            throw new BusinessException(AccountErrorCode.INVALID_USERNAME,
                    "Username must be between " + MIN_LEN + " and " + MAX_LEN
                            + " characters, got " + trimmed.length());
        }

        if (!FORMAT.matcher(trimmed).matches()) {
            throw new BusinessException(AccountErrorCode.INVALID_USERNAME,
                    "Username may only contain letters, digits, and underscores");
        }

        value = trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Username(String value1))) return false;
        return value.equals(value1);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}