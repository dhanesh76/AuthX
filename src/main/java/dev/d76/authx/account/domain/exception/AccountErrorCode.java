package dev.d76.authx.account.domain.exception;

import dev.d76.spring.exception.ErrorCode;

public enum AccountErrorCode implements ErrorCode {
    ACCOUNT_NOT_FOUND(404, "No account is associated with this usernameOrEmail address."),
    INVALID_CREDENTIALS(401, "The provided credentials are invalid"),
    ACCOUNT_ALREADY_ACTIVE(409, "Account is already active"),
    ACCOUNT_NOT_ACTIVE(403, "Account must be active to perform this operation"),
    ACCOUNT_BLOCKED(403, "Account Blocked, cannot continue"),
    ACCOUNT_NOT_VERIFIED(401, "Your usernameOrEmail address has not been verified. A new verification code has been sent."), // ← ADDED
    EMAIL_ALREADY_REGISTERED(409, "An account already exists with this usernameOrEmail"),
    USERNAME_TAKEN(409, "This username is already in use"),
    INVALID_USERNAME(400, "Invalid username"),
    INVALID_PASSWORD(400, "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, a digit, and a special character"),
    SAME_PASSWORD(409, "New password cannot be the same as the current password"),
    PROVIDER_ALREADY_LINKED(409, "This provider is already linked to your account"),
    PROVIDER_NOT_LINKED(409, "This provider is not linked to your account"),
    ACCESS_DENIED(403, "You do not have permission to access this resource."),
    INVALID_TOKEN(401, "Invalid Token"),
    EMAIL_REQUIRED(400, "A valid usernameOrEmail address is required.");

    private final int status;
    private final String message;

    AccountErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override public int getHttpStatus() { return status; }
    @Override public String defaultMessage() { return message; }
}