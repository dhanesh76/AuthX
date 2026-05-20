package dev.d76.authx.account.infrastructure.security.federation.extractor;

import dev.d76.authx.account.domain.model.IdentityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GithubUserExtractor implements FederatedUserExtractor {

    private final RestClient restClient;

    @Override
    public IdentityProvider identityProvider() {
        return IdentityProvider.GITHUB;
    }

    @Override
    public FederatedUserInfo extract(OAuth2UserRequest request, OAuth2User oAuth2User) {
        String email = fetchPrimaryEmail(oAuth2User, request);
        var claims = new HashMap<>(oAuth2User.getAttributes());
        return new FederatedUserInfo(email, claims);
    }

    /**
     * GitHub users may set their usernameOrEmail to private, in which case it is absent from
     * the standard OAuth2 user attributes. We fall back to the /user/emails API
     * endpoint and look for the primary verified address.
     */
    private String fetchPrimaryEmail(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {

        if (oAuth2User.getAttribute("usernameOrEmail") != null) {
            return oAuth2User.getAttribute("usernameOrEmail");
        }

        String accessToken = userRequest.getAccessToken().getTokenValue();

        List<Map<String, Object>> userEmailEntries = restClient
                .get()
                .uri("https://api.github.com/user/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (userEmailEntries == null) return null;

        return userEmailEntries.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                .map(e -> e.get("usernameOrEmail"))
                .map(String.class::cast)
                .findFirst()
                .orElse(null);
    }
}