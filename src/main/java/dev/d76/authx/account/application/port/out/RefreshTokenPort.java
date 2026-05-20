package dev.d76.authx.account.application.port.out;

import dev.d76.spring.exception.BusinessException;

public interface RefreshTokenPort {

    /**
     * Issues a new refresh token for the account.
     * The raw token value is returned to the caller — store it securely.
     * The token hash is persisted server-side for validation.
     */
    String issue(String accountId);

    /**
     * Verifies the raw token is active and returns the associated accountId.
     *
     * @throws BusinessException INVALID_TOKEN if not found, expired, or revoked
     */
    String verify(String rawToken);

    /**
     * Single-use rotation — deletes the old token and issues a new one atomically.
     * Presenting the same token twice returns INVALID_TOKEN on the second attempt,
     * which detects stolen token reuse.
     */
    String rotate(String rawToken, String accountId);

    /**
     * Revokes the token. Silent no-op if the token is not found or already revoked.
     */
    void revoke(String rawToken);

    /**
     * Revokes all tokens for the account, terminating all active sessions.
     * Called after password change and password reset.
     */
    void revokeAll(String accountId);
}