package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.port.in.ForgotPasswordUseCase;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.model.ChallengeType;
import dev.d76.authx.challenge.domain.port.in.IssueChallengeUseCase;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService implements ForgotPasswordUseCase {

    private final AccountRepository accountRepository;
    private final IssueChallengeUseCase issueChallengeUseCase;

    @Override
    public void execute(Email email) {

        // Confirm account exists before issuing OTP —
        // never issue challenges for emails not in the system
        if (!accountRepository.existsByEmail(email)) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }

        issueChallengeUseCase.execute(email, ChallengeType.OTP, ChallengePurpose.PASSWORD_RESET);
    }
}