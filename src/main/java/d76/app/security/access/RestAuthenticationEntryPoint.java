package d76.app.security.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import d76.app.auth.exception.AuthErrorCode;
import d76.app.core.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private final ObjectMapper objectMapper;

    /**
     * Triggered when accessing a protected resource without being logged-in
     * User is anonymous / session expired / no token
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, @NonNull AuthenticationException authenticationException) throws IOException {

        objectMapper.registerModule(new JavaTimeModule());
        AuthErrorCode errorCode = AuthErrorCode.INVALID_CREDENTIALS;
        ApiErrorResponse errorResponse = ApiErrorResponse
                .builder()
                .timestamp(Instant.now())
                .statusCode(errorCode.getStatus().value())
                .errorCode(errorCode.getCode())
                .message(errorCode.defaultMessage())
                .path(request.getRequestURI())
                .build();

        response.setStatus(errorResponse.getStatusCode());
        response.setContentType("application/json");
        response.getWriter().write(
                objectMapper.writeValueAsString(errorResponse)
        );
    }
}
