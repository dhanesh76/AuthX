package dev.d76.authx.account.web.controller;

import dev.d76.authx.account.application.model.dto.LoginResponse;
import dev.d76.authx.account.application.port.in.*;
import dev.d76.authx.account.application.port.in.*;
import dev.d76.authx.account.domain.port.in.ChangePasswordUseCase;
import dev.d76.authx.account.domain.port.in.RegisterUseCase;
import dev.d76.authx.account.domain.vo.RawPassword;
import dev.d76.authx.account.domain.vo.Username;
import dev.d76.authx.account.infrastructure.security.AccountPrincipal;
import dev.d76.authx.account.web.request.*;
import dev.d76.authx.challenge.domain.vo.ChallengeSecret;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.authx.platform.humanverification.HumanVerificationActions;
import dev.d76.authx.platform.humanverification.annotation.RequiresHumanVerification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final FederatedRegisterUseCase federatedRegisterUseCase;
    private final FederatedLinkUseCase federatedLinkUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    // ── Credential auth ──────────────────────────────────────────────────────

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequiresHumanVerification(action = HumanVerificationActions.REGISTER)
    public void register(@Valid @RequestBody RegisterRequest request) {
        registerUseCase.execute(
                new Username(request.username()),
                new Email(request.email()),
                new RawPassword(request.password())
        );
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailUseCase.execute(
                new Email(request.email()),
                new ChallengeSecret(request.secret())
        );
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @RequiresHumanVerification(action = HumanVerificationActions.LOGIN)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return loginUseCase.execute(
                request.usernameOrEmail(),
                new RawPassword(request.password())
        );
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshUseCase.execute(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.execute(request.refreshToken());
    }

    // ── Federated auth ───────────────────────────────────────────────────────

    @PostMapping("/federated/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse federatedRegister(@Valid @RequestBody FederatedRegisterRequest request) {
        return federatedRegisterUseCase.execute(
                request.flowToken(),
                new Username(request.username())
        );
    }

    @PostMapping("/federated/link")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse federatedLink(@Valid @RequestBody FederatedLinkRequest request) {
        return federatedLinkUseCase.execute(request.flowToken());
    }

    // ── Password management ──────────────────────────────────────────────────

    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequiresHumanVerification(action = HumanVerificationActions.PASSWORD_FORGOT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(new Email(request.email()));
    }
    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.OK)
    @RequiresHumanVerification(action = HumanVerificationActions.PASSWORD_RESET)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.execute(
                new Email(request.email()),
                new ChallengeSecret(request.secret()),
                new RawPassword(request.newPassword())
        );
    }

    @PostMapping("/password/change")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(
            @AuthenticationPrincipal AccountPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {

        changePasswordUseCase.execute(
                new Email(principal.getUsername()),
                request.currentPassword(),
                new RawPassword(request.newPassword())
        );
    }
}