package dev.d76.authx.account.application.model.dto;

public record LoginResponse(
        String accountId,
        String email,
        String accessToken,
        String refreshToken
) {}