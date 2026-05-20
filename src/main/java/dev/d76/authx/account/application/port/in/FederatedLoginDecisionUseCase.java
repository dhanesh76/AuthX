package dev.d76.authx.account.application.port.in;

import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.model.IdentityProvider;
import dev.d76.authx.kernel.vo.Email;

public interface FederatedLoginDecisionUseCase {

    FederatedLoginDecision evaluate(Email email, IdentityProvider provider);

    record FederatedLoginDecision(
            Decision decision,
            Email email,
            IdentityProvider provider,
            Account.AccountSnapshot account
    ) {
        public enum Decision {
            ALLOW_LOGIN,
            REQUIRE_REGISTRATION,
            REQUIRE_LINKING,
            BLOCKED
        }
    }
}