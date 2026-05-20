package dev.d76.authx.account.application.port.in;

import dev.d76.authx.account.application.model.dto.LoginResponse;

public interface FederatedLinkUseCase {
    LoginResponse execute(String flowToken);
}