package dev.d76.authx.account.infrastructure.security.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "application.security.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotNull Duration accessTtl,
        @NotNull Duration flowTtl,
        @NotNull Duration reAuthTtl
){}
