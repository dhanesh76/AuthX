package d76.app.security.principal;

import d76.app.auth.model.IdentityProvider;
import d76.app.user.entity.Users;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@Builder(builderClassName = "Builder")
public class UserPrincipal implements UserDetails, OidcUser, OAuth2User {

    private final Long userId;
    private final String email;
    private final IdentityProvider identityProvider;
    private final Collection<? extends GrantedAuthority> authorities;

    private final Map<String, Object> attributes;
    private final OidcIdToken oidcIdToken;
    private final OidcUserInfo oidcUserInfo;

    // -------- Static factory helpers (entry points) --------

    public static UserPrincipal fromUserEntity(Users user) {
        return baseBuilder(user)
                .identityProvider(IdentityProvider.EMAIL)
                .build();
    }

    public static UserPrincipal fromOAuth2(Users user,
                                           IdentityProvider provider,
                                           Map<String, Object> attributes) {
        return baseBuilder(user)
                .identityProvider(provider)
                .attributes(attributes)
                .build();
    }

    public static UserPrincipal fromOidc(Users user,
                                         IdentityProvider provider,
                                         Map<String, Object> attributes,
                                         OidcIdToken idToken,
                                         OidcUserInfo userInfo) {
        return baseBuilder(user)
                .identityProvider(provider)
                .attributes(attributes)
                .oidcIdToken(idToken)
                .oidcUserInfo(userInfo)
                .build();
    }

    public static UserPrincipal fromJwt(Long userId,
                                        String email,
                                        IdentityProvider provider,
                                        Collection<? extends GrantedAuthority> authorities) {
        return UserPrincipal.builder()
                .userId(userId)
                .email(email)
                .identityProvider(provider)
                .authorities(authorities)
                .build();
    }

    // Common builder setup for DB user
    private static Builder baseBuilder(Users user) {
        return UserPrincipal.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .authorities(user.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                        .toList());
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcIdToken != null ? oidcIdToken.getClaims() : Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUserInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcIdToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }

    @Override
    @NonNull
    public String getName() {
        return email;
    }
}
