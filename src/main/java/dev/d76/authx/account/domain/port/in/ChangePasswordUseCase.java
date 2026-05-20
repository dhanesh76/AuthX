package dev.d76.authx.account.domain.port.in;

import dev.d76.authx.account.domain.vo.RawPassword;
import dev.d76.authx.kernel.vo.Email;

public interface ChangePasswordUseCase {
    void execute(Email email, String currentPassword, RawPassword newPassword);
}