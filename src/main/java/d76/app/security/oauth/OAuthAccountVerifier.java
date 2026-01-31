package d76.app.security.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import d76.app.auth.model.IdentityProvider;
import d76.app.user.entity.Users;
import d76.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuthAccountVerifier {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    Users verifyUser(String email, String provider, IdentityProvider expectedProvider) {

        if (email == null) {
            throw oauthError(
                    "email_missing",
                    provider,
                    null,
                    provider + " account has no accessible email"
            );
        }

        Users user = userService.findUserByEmail(email).orElseThrow(() ->
                oauthError(
                        "user_not_registered",
                        provider,
                        email,
                        "No user exists with email: " + email
                )
        );

        if (!user.getIdentityProviders().contains(expectedProvider)) {
            throw oauthError(
                    "auth_provider_not_linked",
                    provider,
                    email,
                    "The email " + email + " is not linked with " + provider + " sign-in."
            );
        }

        return user;
    }

    private OAuth2AuthenticationException oauthError(
            String errorCode,
            String provider,
            String email,
            String message
    ) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("authProvider", provider);

        if (email != null) {
            meta.put("email", email);
        }

        try {
            return new OAuth2AuthenticationException(
                    new OAuth2Error(errorCode, objectMapper.writeValueAsString(meta), null),
                    message
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
