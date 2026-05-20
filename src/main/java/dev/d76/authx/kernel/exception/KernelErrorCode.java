package dev.d76.authx.kernel.exception;

import dev.d76.spring.exception.ErrorCode;

public enum KernelErrorCode implements ErrorCode {
    INVALID_EMAIL(400, "Invalid usernameOrEmail format"),
    VERIFICATION_TOKEN_MISSING(400, "Request verification token is missing."),
    HUMAN_VERIFICATION_FAILED(400, "Request verification failed. Please try again.");

    private final int status;
    private final String message;

    KernelErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override public int getHttpStatus() { return status; }
    @Override public String defaultMessage() { return message; }
}