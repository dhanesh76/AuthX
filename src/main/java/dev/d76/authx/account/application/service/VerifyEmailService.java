package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.port.in.VerifyEmailUseCase;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.model.ChallengeType;
import dev.d76.authx.challenge.domain.port.in.VerifyChallengeUseCase;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {

    private final AccountRepository accountRepository;
    private final VerifyChallengeUseCase verifyChallengeUseCase;

    @Override
    @Transactional
    public void execute(Email email, ChallengeSecret attempted) {

        // OTP verified first — fail fast before loading the account
        verifyChallengeUseCase
                .execute(email, attempted, ChallengePurpose.EMAIL_VERIFICATION, ChallengeType.OTP);

        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));

        account.activate();
        accountRepository.save(account);
    }
}