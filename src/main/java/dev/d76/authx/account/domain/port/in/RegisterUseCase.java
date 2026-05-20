package dev.d76.authx.account.domain.port.in;

import dev.d76.authx.account.domain.vo.RawPassword;
import dev.d76.authx.account.domain.vo.Username;
import dev.d76.authx.kernel.vo.Email;

public interface RegisterUseCase {
    void execute(Username username, Email email, RawPassword password);
}