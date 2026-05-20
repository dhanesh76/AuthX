package dev.d76.authx.account.application.port.in;

import dev.d76.authx.kernel.vo.Email;

public interface ForgotPasswordUseCase {
    void execute(Email email);
}