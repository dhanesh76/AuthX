package dev.d76.authx.platform.ratelimit.exception;

import dev.d76.spring.exception.BusinessException;

public class RateLimitExceededException extends BusinessException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds){
        super(RateLimitErrorCode.RATE_LIMIT_EXCEEDED);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    public long retryAfterSeconds() { return retryAfterSeconds; }
}