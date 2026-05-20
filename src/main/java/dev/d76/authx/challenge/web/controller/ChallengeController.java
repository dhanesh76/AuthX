package dev.d76.authx.challenge.web.controller;

import dev.d76.authx.challenge.domain.port.in.IssueChallengeUseCase;
import dev.d76.authx.challenge.web.request.IssueRequest;
import dev.d76.authx.kernel.vo.Email;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final IssueChallengeUseCase challengeUseCase;

    /**
     * Issues a challenge to the given usernameOrEmail address.
     * Any previously pending challenge for the same usernameOrEmail and purpose is invalidated first —
     * only one active challenge exists per usernameOrEmail per purpose at any time.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void issue(@Valid @RequestBody IssueRequest request) {
        challengeUseCase.execute(
                new Email(request.email()),
                request.type(),
                request.purpose()
        );
    }
}