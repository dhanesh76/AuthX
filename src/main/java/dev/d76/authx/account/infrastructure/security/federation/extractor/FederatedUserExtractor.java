package dev.d76.authx.account.infrastructure.security.federation.extractor;

import dev.d76.authx.account.domain.model.IdentityProvider;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface FederatedUserExtractor {
    IdentityProvider identityProvider();
    FederatedUserInfo extract(OAuth2UserRequest request, OAuth2User user);
}
