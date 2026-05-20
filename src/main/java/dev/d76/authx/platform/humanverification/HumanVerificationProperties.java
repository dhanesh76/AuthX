package dev.d76.authx.platform.humanverification;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.human-verification")
public record HumanVerificationProperties(
        @NotBlank String secretKey,
        @NotBlank String verifyUrl,
        @NotBlank String expectedHost
) {
}
