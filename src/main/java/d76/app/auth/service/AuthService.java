package d76.app.auth.service;

import d76.app.auth.dto.*;
import d76.app.auth.exception.AuthErrorCode;
import d76.app.auth.model.IdentityProvider;
import d76.app.core.exception.BusinessException;
import d76.app.core.service.CacheService;
import d76.app.notification.email.service.MailService;
import d76.app.notification.otp.model.OtpPurpose;
import d76.app.notification.otp.service.OtpService;
import d76.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final OtpService otpService;
    private final MailService mailService;
    private final CacheService cacheService;


    public void register(RegisterRequest request) {
        userService.assertUsernameAvailable(request.userName());
        userService.assertEmailAvailable(request.email());

        var otpPurpose = OtpPurpose.EMAIL_VERIFICATION;

        var otp = otpService.issueOtp(request.email(), otpPurpose);
        mailService.sendTextMail(request.email(), otpPurpose.subject(), otpPurpose.body(otp));
    }

    public RegisterResponse verifyOtp(OtpVerifyRequest request) {
        otpService.verifyOtp(getTempRegisterKey(request.email()), request.otp(), OtpPurpose.EMAIL_VERIFICATION);

        var tempUser = cacheService.get(getTempRegisterKey(request.email()), TempUser.class).orElseThrow(
                () -> new BusinessException(AuthErrorCode.REGISTER_SESSION_EXPIRED)
        );

        var user = userService.createLocalUser(tempUser.email(), tempUser.username(), tempUser.password());
        return new RegisterResponse(user.getEmail(), IdentityProvider.EMAIL.name(), Instant.now());
    }

    public void forgotPassword(String email) {

        //validate the userExists
        userService.assertUserExistByEmail(email);

        var otpPurpose = OtpPurpose.PASSWORD_RESET;

        String otp = otpService.issueOtp(email, otpPurpose);
        mailService.sendTextMail(email, otpPurpose.subject(), otpPurpose.body(otp));
    }

    public void resetPassword(ResetPasswordRequest request) {
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.PASSWORD_RESET);
        userService.updatePassword(request.email(), request.newPassword());
    }

    public String getTempRegisterKey(String email) {
        String TEMP_REGISTER_PREFIX = "register:temp:email:";
        return TEMP_REGISTER_PREFIX + email;
    }

    public void requestOtp(OtpRequest otpRequest) {

        //inappropriate action
        if (OtpPurpose.EMAIL_VERIFICATION.equals(otpRequest.purpose())) {
            userService.assertEmailAvailable(otpRequest.email());
        }
        otpService.issueOtp(otpRequest.email(), otpRequest.purpose());
    }
}
