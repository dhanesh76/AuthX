package dev.d76.authx.account.infrastructure.security.federation.extractor;

import dev.d76.authx.account.domain.model.IdentityProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FederatedUserExtractorRegistry {

    private final Map<IdentityProvider, FederatedUserExtractor> oAuthUserExtractors;

    public FederatedUserExtractorRegistry(List<FederatedUserExtractor> federatedUserExtractors) {
        this.oAuthUserExtractors = federatedUserExtractors
                .stream()
                .collect(Collectors
                        .toMap(FederatedUserExtractor::identityProvider, Function.identity())
                );
    }

    public FederatedUserExtractor resolve(IdentityProvider provider) {
        FederatedUserExtractor extractor = oAuthUserExtractors.get(provider);
        if (extractor == null) {
            throw new IllegalStateException(
                    "No OAuth extractor registered for provider: " + provider
            );
        }
        return extractor;
    }
}
