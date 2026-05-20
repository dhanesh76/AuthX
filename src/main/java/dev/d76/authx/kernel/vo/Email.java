package dev.d76.authx.kernel.vo;

import dev.d76.authx.kernel.exception.KernelErrorCode;
import dev.d76.spring.exception.BusinessException;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

@ValueObject
public record Email(String value) {

    private static final Pattern FORMAT = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    public Email {
        Objects.requireNonNull(value, "Email must not be null");

        String normalized = value.strip().toLowerCase();
        if (!FORMAT.matcher(normalized).matches()) {
            throw new BusinessException(KernelErrorCode.INVALID_EMAIL,
                    "Invalid usernameOrEmail format: '" + normalized + "'");
        }
        value = normalized;
    }
}