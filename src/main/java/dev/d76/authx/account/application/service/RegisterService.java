package dev.d76.authx.account.application.service;

import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.port.in.RegisterUseCase;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.account.domain.port.out.PasswordEncoder;
import dev.d76.authx.account.domain.vo.RawPassword;
import dev.d76.authx.account.domain.vo.Username;
import dev.d76.authx.challenge.domain.model.ChallengePurpose;
import dev.d76.authx.challenge.domain.model.ChallengeType;
import dev.d76.authx.challenge.domain.port.in.IssueChallengeUseCase;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {

    private final AccountRepository accountRepository;
    private final IssueChallengeUseCase challengeUseCase;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void execute(Username username, Email email, RawPassword password) {

        if (accountRepository.existsByEmail(email)) {
            throw new BusinessException(AccountErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        if (accountRepository.existsByUsername(username)) {
            throw new BusinessException(AccountErrorCode.USERNAME_TAKEN);
        }

        Account account = Account.createPendingAccount(
                username, email, password, encoder, Instant.now()
        );

        accountRepository.save(account);

        // OTP is issued outside the @Transactional boundary intentionally.
        // If mail dispatch fails the account is already persisted — the client
        // can request a resend. Rolling back account creation for a mail failure
        // would force the user to re-register unnecessarily.
        challengeUseCase.execute(email, ChallengeType.OTP, ChallengePurpose.EMAIL_VERIFICATION);
    }
}