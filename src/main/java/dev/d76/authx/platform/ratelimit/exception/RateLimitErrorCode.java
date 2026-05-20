package dev.d76.authx.platform.ratelimit.exception;

import dev.d76.spring.exception.ErrorCode;

public enum RateLimitErrorCode implements ErrorCode {
    RATE_LIMIT_EXCEEDED(429, "Too many requests, try after some time");

    RateLimitErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
    @Override
    public int getHttpStatus() {
        return status;
    }

    @Override
    public String defaultMessage() {
        return message;
    }

    private final int status;
    private final String message;
}
