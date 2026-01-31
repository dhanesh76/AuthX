package d76.app.security.oauth;

import d76.app.auth.model.IdentityProvider;
import d76.app.security.principal.UserPrincipal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final RestClient restClient;
    private final OAuthAccountVerifier authAccountVerifier;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        var delegate = new DefaultOAuth2UserService();
        var oAuth2User = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = fetchPrimaryEmail(userRequest, oAuth2User);

        var user = authAccountVerifier.verifyUser(email, provider, IdentityProvider.GITHUB);

        var attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("email", email);

        return UserPrincipal.fromOAuth2(user, IdentityProvider.GITHUB, attributes);
    }

    @Nullable
    private String fetchPrimaryEmail(OAuth2UserRequest request, OAuth2User oAuth2User) {

        String email = oAuth2User.getAttribute("email");
        if (email != null) return email;

        String token = request.getAccessToken().getTokenValue();

        var emails = restClient
                .get()
                .uri("https://api.github.com/user/emails")
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(new ParameterizedTypeReference<@NonNull List<Map<String, Object>>>() {
                });

        return emails == null ? null : emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                .map(e -> (String) e.get("email"))
                .findFirst()
                .orElse(null);
    }
}
