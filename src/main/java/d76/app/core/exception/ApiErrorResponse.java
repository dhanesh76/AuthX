package d76.app.core.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiErrorResponse {
    private Instant timestamp;
    private int statusCode;
    private String errorCode;
    private String message;
    private String path;
    private List<ApiFieldError> errors;

    private String authProvider;
    private String actionToken;

    public static ApiErrorResponse constructErrorResponse(ErrorCode code, HttpServletRequest request) {
        return ApiErrorResponse
                .builder()
                .timestamp(Instant.now())
                .errorCode(code.getCode())
                .statusCode(code.getStatus().value())
                .message(code.defaultMessage())
                .path(request.getRequestURI())
                .build();
    }

    public static ApiErrorResponse constructErrorResponse(ErrorCode code, String message, HttpServletRequest request) {
        return ApiErrorResponse
                .builder()
                .timestamp(Instant.now())
                .errorCode(code.getCode())
                .statusCode(code.getStatus().value())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }

    @Data
    @AllArgsConstructor
    public static class ApiFieldError {
        private String field;
        private String message;
    }
}
