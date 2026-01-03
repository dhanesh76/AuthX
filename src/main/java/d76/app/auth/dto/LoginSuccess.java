package d76.app.auth.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record LoginSuccess(
        String status,
        String username,
        String accessToken,
        String authProvider,
        Instant issuedAt
) {
}

