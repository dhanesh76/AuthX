package dev.d76.authx.account.infrastructure.security.federation.exception;

import dev.d76.authx.account.application.port.in.FederatedLoginDecisionUseCase.FederatedLoginDecision;
import dev.d76.authx.account.domain.model.IdentityProvider;
import org.springframework.security.core.AuthenticationException;


/**
 * Thrown from the OAuth user services when the provider callback succeeds
 * but the account requires additional action before a session can be granted.
 * <p>
 * Extends AuthenticationException so Spring's OAuth2 filter chain catches it
 * and routes the request to LoginFailureHandler &mdash; not to a 500 error page.
 */
public class FederatedLoginException extends AuthenticationException {

    private final FederatedLoginDecision.Decision decision;
    private final String email;
    private final IdentityProvider provider;

    public FederatedLoginException(FederatedLoginDecision.Decision decision, String email, IdentityProvider provider) {
        super("OAuth flow required: " + decision.name());

        this.decision = decision;
        this.email = email;
        this.provider = provider;
    }

    public FederatedLoginDecision.Decision decision() {
        return decision;
    }

    public String email() {
        return email;
    }

    public IdentityProvider provider() {
        return provider;
    }
}