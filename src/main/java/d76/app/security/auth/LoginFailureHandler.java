package d76.app.security.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import d76.app.auth.exception.AuthErrorCode;
import d76.app.core.exception.ApiErrorResponse;
import d76.app.security.jwt.JwtService;
import d76.app.security.jwt.model.JwtPurpose;
import d76.app.user.exception.UserErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@NullMarked
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse res, AuthenticationException ex) throws IOException {

        if (ex instanceof OAuth2AuthenticationException oae) {
            var oaeError = oae.getError();

            String desc = oae.getError().getDescription();

            Map<String, String> meta = Map.of();
            if (desc != null)
                meta = objectMapper.readValue(desc, new TypeReference<>() {
                });

            String authProvider = meta.get("identityProvider");
            String email = meta.get("email");

            switch (oaeError.getErrorCode()) {
                case "email_missing" -> {
                    var statusValue = HttpStatus.BAD_REQUEST.value();

                    res.setStatus(statusValue);
                    res.setContentType("application/json");

                    ApiErrorResponse response = ApiErrorResponse.builder().errorCode(AuthErrorCode.EMAIL_REQUIRED.name()).statusCode(statusValue).message(ex.getMessage()).timestamp(Instant.now()).path(request.getRequestURI()).authProvider(authProvider).build();

                    res.getWriter().write(objectMapper.writeValueAsString(response));
                    return;
                }

                case "user_not_registered" -> {

                    var actionToken = jwtService.generateActionToken(email, JwtPurpose.SOCIAL_REGISTER, authProvider);
                    var statusValue = HttpStatus.CONFLICT.value();

                    res.setStatus(statusValue);
                    res.setContentType("application/json");

                    ApiErrorResponse response = ApiErrorResponse.builder().errorCode(UserErrorCode.USER_NOT_FOUND.name()).statusCode(statusValue).message(ex.getMessage()).timestamp(Instant.now()).path(request.getRequestURI()).authProvider(authProvider).actionToken(actionToken).build();
                    res.getWriter().write(objectMapper.writeValueAsString(response));
                    return;
                }

                case "auth_provider_not_linked" -> {
                    var actionToken = jwtService.generateActionToken(email, JwtPurpose.LINK_SOCIAL_ACCOUNT, authProvider);
                    var statusValue = HttpStatus.CONFLICT.value();

                    res.setStatus(statusValue);
                    res.setContentType("application/json");

                    ApiErrorResponse response = ApiErrorResponse.builder().errorCode(AuthErrorCode.AUTH_PROVIDER_NOT_LINKED.name()).statusCode(statusValue).message(meta.get("message")).timestamp(Instant.now()).path(request.getRequestURI()).authProvider(authProvider).actionToken(actionToken).build();

                    res.getWriter().write(objectMapper.writeValueAsString(response));
                    return;
                }
            }
        }

        var errorCode = AuthErrorCode.INVALID_CREDENTIALS;
        ApiErrorResponse errorResponse = ApiErrorResponse.builder().statusCode(errorCode.getStatus().value()).errorCode(errorCode.getCode()).message(errorCode.defaultMessage()).path(request.getRequestURI()).timestamp(Instant.now()).authProvider("EMAIL").build();

        res.setStatus(errorCode.getStatus().value());
        res.setContentType("application/json");
        res.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
