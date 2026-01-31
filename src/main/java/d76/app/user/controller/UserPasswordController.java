package d76.app.user.controller;

import d76.app.user.dto.password.ChangePasswordRequest;
import d76.app.user.dto.password.VerifyPasswordRequest;
import d76.app.user.dto.password.VerifyPasswordResponse;
import d76.app.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/password")
@NullMarked
public class UserPasswordController {

    private final UserService userService;

    @PostMapping("/verify")
    ResponseEntity<VerifyPasswordResponse> verifyPassword(@AuthenticationPrincipal UserDetails principal,
                                                          @RequestBody @Valid VerifyPasswordRequest request) {

        var response = userService.verify(principal.getUsername(), request.password());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/change")
    @ResponseStatus(code = HttpStatus.OK)
    void changePassword(@AuthenticationPrincipal UserDetails principal, @RequestBody @Valid ChangePasswordRequest request) {
        userService.updatePassword(principal.getUsername(), request);
    }
}
