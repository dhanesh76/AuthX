package dev.d76.authx.platform.humanverification;

/**
 * Port for human verification.
 */
public interface HumanVerifier {

    /**
     * Verifies that the token represents a genuine human interaction.
     * <p>
     * token          — raw token from the client's X-Verification-Token header.
     * expectedAction — the action this endpoint declared. Implementations
     * must reject tokens whose recorded action does not match.
     * This prevents a token from one page being replayed
     * on a different endpoint.
     */
    boolean verify(HumanVerificationRequest request);
}