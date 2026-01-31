package d76.app.security.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import d76.app.auth.exception.AuthErrorCode;
import d76.app.core.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@NullMarked
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * Triggered when accessing a resource without permission
     * User is authenticated, but lacks role/authority
     * level: Filter Chain
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        var errorCode = AuthErrorCode.ACCESS_DENIED;

        var errorResponse = ApiErrorResponse.constructErrorResponse(errorCode, request);

        response.setStatus(errorResponse.getStatusCode());
        response.setContentType("application/json");
        response.getWriter().write(
                objectMapper.writeValueAsString(errorResponse)
        );
    }
}
