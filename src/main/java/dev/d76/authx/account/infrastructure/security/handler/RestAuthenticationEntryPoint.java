package dev.d76.authx.account.infrastructure.security.handler;

import dev.d76.authx.account.domain.exception.AccountErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.d76.spring.exception.autoconfigure.web.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@NullMarked
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Handles requests that arrive without a valid authentication token.
     * Triggered for anonymous users, expired sessions, and missing or malformed tokens.
     * Runs inside the Spring Security filter chain, before the request reaches any controller.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException {

        AccountErrorCode errorCode = AccountErrorCode.INVALID_CREDENTIALS;
        var errorResponse = ApiErrorResponse.builderFrom(errorCode, request).build();

        response.setStatus(errorResponse.httpStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON.getType());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
