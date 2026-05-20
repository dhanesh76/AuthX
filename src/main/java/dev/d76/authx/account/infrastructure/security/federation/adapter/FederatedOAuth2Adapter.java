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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FederatedOAuth2Adapter extends DefaultOAuth2UserService {

    private final FederatedUserExtractorRegistry registry;
    private final FederatedLoginDecisionUseCase decisionUseCase;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oauth2User = super.loadUser(userRequest);

        IdentityProvider provider = IdentityProvider.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );

        FederatedUserInfo userInfo =
                registry.resolve(provider).extract(userRequest, oauth2User);

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
                    decision.account()
            );
        }

        throw new FederatedLoginException(
                decision.decision(),
                decision.email().value(),
                provider
        );
    }
}