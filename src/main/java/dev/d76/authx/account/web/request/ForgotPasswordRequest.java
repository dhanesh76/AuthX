package dev.d76.authx.account.web.request;

import jakarta.validation.constraints.NotBlank;

public  record ForgotPasswordRequest(
        @NotBlank String email
) {}
