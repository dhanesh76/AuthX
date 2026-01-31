package d76.app.auth.controller;

import d76.app.auth.dto.*;
import d76.app.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@NullMarked
@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    void register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.OK)
    void forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
    }

    @PostMapping("/verify/otp")
    ResponseEntity<RegisterResponse> verifyOtp(@RequestBody @Valid OtpVerifyRequest request) {
        var response = authService.verifyOtp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/otp")
    @ResponseStatus(code = HttpStatus.OK)
    void requestOtp(@RequestBody @Valid OtpRequest otpRequest) {
        authService.requestOtp(otpRequest);
    }

    @PostMapping("/password/reset")
    @ResponseStatus(code = HttpStatus.OK)
    void resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
    }
}
