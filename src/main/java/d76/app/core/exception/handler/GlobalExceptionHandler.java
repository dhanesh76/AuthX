package d76.app.core.exception.handler;

import d76.app.core.exception.ApiErrorResponse;
import d76.app.core.exception.BusinessException;
import d76.app.core.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@NullMarked
@RestControllerAdvice(annotations = RestController.class)
public final class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex,
                                                             HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();

        ApiErrorResponse response = ApiErrorResponse
                .builder()
                .statusCode(errorCode.getStatus().value())
                .errorCode(errorCode.getCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        List<ApiErrorResponse.ApiFieldError> fieldErrors = ex
                .getBindingResult().getFieldErrors()
                .stream().map(fieldError ->
                        new ApiErrorResponse.ApiFieldError(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                )
                .collect(Collectors.toList());

        ApiErrorResponse response = ApiErrorResponse
                .builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .message("Validation Failed")
                .path(request.getRequestURI())
                .errors(fieldErrors)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleException(HttpServletRequest request) {

        ApiErrorResponse response = ApiErrorResponse
                .builder()
                .timestamp(Instant.now())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode("INTERNAL_ERROR")
                .message("Unexpected error occurred")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
    }
}
