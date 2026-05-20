package dev.d76.authx.account.application.port.in;

import dev.d76.authx.account.application.model.dto.LoginResponse;
import dev.d76.authx.account.domain.vo.RawPassword;

public interface LoginUseCase {
    LoginResponse execute(String identifier, RawPassword attempted);
}
