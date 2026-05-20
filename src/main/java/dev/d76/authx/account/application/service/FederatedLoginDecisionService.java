package dev.d76.authx.account.application.service;

import dev.d76.authx.account.application.port.in.FederatedLoginDecisionUseCase;
import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.model.AccountStatus;
import dev.d76.authx.account.domain.model.IdentityProvider;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.kernel.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FederatedLoginDecisionService implements FederatedLoginDecisionUseCase {

    private final AccountRepository accountRepository;

    @Override
    public FederatedLoginDecision evaluate(Email email, IdentityProvider provider) {

        var accountOpt = accountRepository.findByEmail(email);

        if (accountOpt.isEmpty()) {

            return constructDecision(
                    FederatedLoginDecision.Decision.REQUIRE_REGISTRATION, email,
                    provider, null
            );
        }

        Account account = accountOpt.get();
        var snap = account.snapshot();

        if (snap.status() == AccountStatus.BLOCKED) {

            return constructDecision(
                    FederatedLoginDecision.Decision.BLOCKED, email,
                    provider, snap
            );
        }

        if (!account.hasIdentityProvider(provider)) {

            return constructDecision(
                    FederatedLoginDecision.Decision.REQUIRE_LINKING, email,
                    provider, snap
            );
        }

        return constructDecision(
                FederatedLoginDecision.Decision.ALLOW_LOGIN, email,
                provider, snap
        );
    }

    private FederatedLoginDecision constructDecision(
            FederatedLoginDecision.Decision decision,
            Email email, IdentityProvider provider,
            Account.AccountSnapshot snap) {

        return new FederatedLoginDecision(decision, email, provider, snap);
    }
}