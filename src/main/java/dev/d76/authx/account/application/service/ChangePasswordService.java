package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.port.out.RefreshTokenPort;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.port.in.ChangePasswordUseCase;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.account.domain.port.out.PasswordEncoder;
import dev.d76.authx.account.domain.vo.RawPassword;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenPort refreshTokenPort;

    @Override
    @Transactional
    public void execute(Email email, String currentPassword, RawPassword newPassword) {

        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));

        account.verifyPassword(currentPassword, passwordEncoder);
        account.changePassword(newPassword, passwordEncoder);
        accountRepository.save(account);

        // Revoke all sessions after password change — all existing tokens are untrusted
        refreshTokenPort.revokeAll(account.snapshot().accountId().toString());
    }
}