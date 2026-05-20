package dev.d76.authx.platform.humanverification;

import org.jspecify.annotations.Nullable;

public record HumanVerificationRequest(
        String token,
        String expectedAction,
        @Nullable String remoteIp
) {}