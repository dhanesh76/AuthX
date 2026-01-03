package d76.app.core.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getStatus();

    String defaultMessage();

    default String getCode() {
        return ((Enum<?>) this).name();
    }
}
