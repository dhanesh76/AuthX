package dev.d76.authx.account.application.port.in;

public interface LogoutUseCase {
    void execute(String rawRefreshToken);
}