package dev.d76.authx.challenge.infrastructure.sender;

import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;

public record ChallengeEmailTemplate(String subject, String body) {

    public static ChallengeEmailTemplate of(ChallengePurpose purpose, ChallengeSecret secret) {
        return switch (purpose) {
            case EMAIL_VERIFICATION -> new ChallengeEmailTemplate(
                    "Verify your account",
                    "Your verification code is: " + secret.value() +
                            "\nThis code expires in 10 minutes."
            );
            case PASSWORD_RESET -> new ChallengeEmailTemplate(
                    "Reset your password",
                    "Your password reset code is: " + secret.value() +
                            "\nThis code expires in 10 minutes. If you didn't request this, ignore this usernameOrEmail."
            );
        };
    }
}