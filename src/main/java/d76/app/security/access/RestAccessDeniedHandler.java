package d76.app.security.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import d76.app.auth.exception.AuthErrorCode;
import d76.app.core.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;


@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * Triggered when accessing a resource without permission
     * User is authenticated, but lacks role/authority
     * level: Filter Chain
     *
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        AuthErrorCode errorCode = AuthErrorCode.ACCESS_DENIED;
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
