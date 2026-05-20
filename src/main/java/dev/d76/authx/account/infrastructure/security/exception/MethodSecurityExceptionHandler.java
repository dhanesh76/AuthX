package dev.d76.authx.account.infrastructure.security.exception;

import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.spring.exception.autoconfigure.web.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@NullMarked
@Order(value = 1)
public class MethodSecurityExceptionHandler {

    /**
     * Handles @PreAuthorize and @Secured denials for authenticated users
     * who lack the required role or authority.
     * Complements RestAccessDeniedHandler which handles filter-chain-level denials.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex, HttpServletRequest request) {

        AccountErrorCode errorCode = AccountErrorCode.ACCESS_DENIED;
        var errorResponse = ApiErrorResponse.builderFrom(errorCode, request).build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(errorResponse);
    }
}
