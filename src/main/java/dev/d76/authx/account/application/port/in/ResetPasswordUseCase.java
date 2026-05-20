package dev.d76.authx.account.application.port.in;

import dev.d76.authx.account.domain.vo.RawPassword;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import dev.d76.authx.kernel.vo.Email;

public interface ResetPasswordUseCase {
    void execute(Email email, ChallengeSecret otp, RawPassword newPassword);
}