package dev.d76.authx.account.web.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String password
) {}