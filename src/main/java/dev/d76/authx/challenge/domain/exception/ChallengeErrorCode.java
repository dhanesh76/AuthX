package dev.d76.authx.challenge.domain.exception;

import dev.d76.spring.exception.ErrorCode;

public enum ChallengeErrorCode implements ErrorCode {

    CHALLENGE_NOT_PENDING(409, "Challenge is not in a pending state"),
    CHALLENGE_EXPIRED(408, "Verification code has expired, please request a new one"),
    INVALID_CHALLENGE_SECRET(400, "Verification code is incorrect"),
    CHALLENGE_NOT_FOUND(404, "No active challenge found for this usernameOrEmail and purpose."),;

    private final int status;
    private final String message;

    ChallengeErrorCode(int status, String message) {
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
}
