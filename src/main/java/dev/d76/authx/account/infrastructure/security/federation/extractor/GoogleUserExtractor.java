package dev.d76.authx.account.infrastructure.security.federation.extractor;


import dev.d76.authx.account.domain.model.IdentityProvider;
import dev.d76.authx.account.infrastructure.security.token.jwt.IdentityAttributes;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleUserExtractor implements FederatedUserExtractor {

    @Override
    public IdentityProvider identityProvider() {
        return IdentityProvider.GOOGLE;
    }

    @Override
    public FederatedUserInfo extract(OAuth2UserRequest request, OAuth2User oAuth2User) {
        if (oAuth2User instanceof OidcUser oidcUser) {
            return new FederatedUserInfo(oidcUser.getEmail(), oidcUser.getAttributes());
        }
        String email = oAuth2User.getAttribute(IdentityAttributes.EMAIL);
        return new FederatedUserInfo(email, oAuth2User.getAttributes());
    }
}