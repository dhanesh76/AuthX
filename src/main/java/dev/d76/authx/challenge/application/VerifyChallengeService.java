package dev.d76.authx.challenge.application;

import dev.d76.authx.challenge.domain.exception.ChallengeErrorCode;
import dev.d76.authx.challenge.domain.model.Challenge;
import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.model.ChallengeType;
import dev.d76.authx.challenge.domain.port.in.VerifyChallengeUseCase;
import dev.d76.authx.challenge.domain.port.out.ChallengeRepository;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VerifyChallengeService implements VerifyChallengeUseCase {

    private final ChallengeRepository repository;

    @Override
    public void execute(Email email, ChallengeSecret attempted, ChallengePurpose issuedFor, ChallengeType type) {

        Challenge challenge = repository
                .findLatestPending(email, issuedFor)
                .orElseThrow(() ->
                        new BusinessException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

        challenge.verify(attempted, issuedFor, Instant.now());
        repository.deleteAll(email, issuedFor);
    }
}
