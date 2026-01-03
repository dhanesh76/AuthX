package d76.app.security.exception;

import d76.app.auth.exception.AuthErrorCode;
import d76.app.core.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Order(value = 1)
@NullMarked
public class MethodSecurityExceptionHandler {

    /**
     * Triggered when accessing a resource without permission
     * User is authenticated, but lacks role/authority
     * level: method level
     *
     */

    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex, HttpServletRequest request) {

        AuthErrorCode errorCode = AuthErrorCode.ACCESS_DENIED;
        ApiErrorResponse errorResponse = ApiErrorResponse
                .builder()
                .timestamp(Instant.now())
                .statusCode(errorCode.getStatus().value())
                .errorCode(errorCode.getCode())
                .message(errorCode.defaultMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(errorCode.getStatus().value()).body(errorResponse);
    }
}
