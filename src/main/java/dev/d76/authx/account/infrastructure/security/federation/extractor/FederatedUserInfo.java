package dev.d76.authx.account.infrastructure.security.federation.extractor;

import java.util.Map;

public record FederatedUserInfo(
        String email,
        Map<String, Object> claims
) {
}
