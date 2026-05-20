package dev.d76.authx.account.infrastructure.security.federation.adapter;

import dev.d76.authx.account.application.port.in.FederatedLoginDecisionUseCase;
import dev.d76.authx.account.application.port.in.FederatedLoginDecisionUseCase.FederatedLoginDecision.Decision;
import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.model.IdentityProvider;
import dev.d76.authx.account.infrastructure.security.federation.FederatedPrincipal;
import dev.d76.authx.account.infrastructure.security.federation.exception.FederatedLoginException;
import dev.d76.authx.account.infrastructure.security.federation.extractor.FederatedUserExtractorRegistry;
import dev.d76.authx.account.infrastructure.security.federation.extractor.FederatedUserInfo;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FederatedOidcAdapter extends OidcUserService {

    private final FederatedUserExtractorRegistry registry;
    private final FederatedLoginDecisionUseCase decisionUseCase;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {

        OidcUser oidcUser = super.loadUser(userRequest);

        IdentityProvider provider = IdentityProvider.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );

        FederatedUserInfo userInfo =
                registry.resolve(provider).extract(userRequest, oidcUser);

        if (userInfo.email() == null) {
            throw new BusinessException(AccountErrorCode.EMAIL_REQUIRED);
        }

        var decision = decisionUseCase.evaluate(
                new Email(userInfo.email()), provider
        );

        if (decision.decision() == Decision.ALLOW_LOGIN) {
            return new FederatedPrincipal(
                    decision.email().value(),
                    provider,
                    decision.account(),
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo()
            );
        }

        throw new FederatedLoginException(
                decision.decision(),
                decision.email().value(),
                provider
        );
    }
}