package dev.d76.authx.account.application.port.in;

import dev.d76.authx.account.application.model.dto.LoginResponse;
import dev.d76.authx.account.domain.vo.Username;

public interface FederatedRegisterUseCase {
    LoginResponse execute(String flowToken, Username username);
}