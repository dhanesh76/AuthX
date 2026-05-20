package dev.d76.authx.account.web.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String email,
        @NotBlank String secret,
        @NotBlank String newPassword
) {}

