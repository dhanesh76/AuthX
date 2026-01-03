package d76.app.auth.service;

import d76.app.auth.dto.RegisterRequest;
import d76.app.auth.dto.RegisterResponse;
import d76.app.auth.dto.ResetPasswordRequest;
import d76.app.auth.model.AuthProvider;
import d76.app.core.notification.email.MailService;
import d76.app.core.notification.otp.model.OtpPurpose;
import d76.app.core.notification.otp.service.OtpService;
import d76.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final OtpService otpService;
    private final MailService mailService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        var user = userService.createLocalUser(request.email(), request.userName(), request.password());
        return new RegisterResponse(user.getEmail(), AuthProvider.EMAIL.name(), user.getCreatedAt());
    }

    @Transactional
    public void forgotPassword(String email) {

        //validate the userExists
        userService.assertUserExistByEmail(email);

        var otpPurpose = OtpPurpose.PASSWORD_RESET;

        String otp = otpService.issueOtp(email, otpPurpose);
        mailService.sendMail(email, otpPurpose.subject(), otpPurpose.body(otp));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.PASSWORD_RESET);

        userService.updatePassword(request.email(), request.newPassword());
    }
}
