package dev.d76.authx.challenge.web.request;

import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.model.ChallengeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IssueRequest(

        @NotBlank
        @jakarta.validation.constraints.Email
        String email,

        @NotNull
        ChallengeType type,

        @NotNull
        ChallengePurpose purpose
) {}