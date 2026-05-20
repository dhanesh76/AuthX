package dev.d76.authx.account.infrastructure.security.federation;

import dev.d76.authx.account.domain.model.Account.AccountSnapshot;
import dev.d76.authx.account.domain.model.IdentityProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record FederatedPrincipal(
        String email,
        IdentityProvider provider,
        @Nullable AccountSnapshot accountSnapshot,
        @Nullable OidcIdToken idToken,
        @Nullable OidcUserInfo userInfo
) implements OidcUser {

    public FederatedPrincipal(String email,
                              IdentityProvider provider,
                              AccountSnapshot accountSnapshot) {
        this(email, provider, accountSnapshot, null, null);
    }

    @Override public Map<String, Object> getClaims() { return Map.of(); }
    @Override public OidcIdToken getIdToken() { return idToken; }
    @Override public OidcUserInfo getUserInfo() { return userInfo; }
    @Override public Map<String, Object> getAttributes() { return Map.of(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
    @Override @NonNull public String getName() { return email; }
}