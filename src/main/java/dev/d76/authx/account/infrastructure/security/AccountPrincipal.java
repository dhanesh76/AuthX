package dev.d76.authx.account.infrastructure.security;

import dev.d76.authx.account.domain.model.AccountStatus;
import dev.d76.authx.account.infrastructure.security.filter.JwtFilter;
import jakarta.annotation.Nullable;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class AccountPrincipal implements UserDetails {

    private final UUID accountId;
    private final String email;
    private final String password;
    private final AccountStatus status;
    private final List<SimpleGrantedAuthority> authorities;

    // ── Construction paths ───────────────────────────────────────────────────

    /**
     * Builds the principal from a database-loaded account snapshot.
     * Used by {@link AccountUserDetailsService} during credential authentication.
     */
    public static AccountPrincipal fromSnapshot(
            UUID accountId,
            String email,
            @Nullable String password,
            AccountStatus status,
            List<String> roles) {
        return new AccountPrincipal(accountId, email, password, status, roles);
    }

    /**
     * Builds the principal from verified JWT claims in {@link JwtFilter}.
     * Password and status are not needed after token verification — the token's
     * existence proves the account was active at login time.
     */
    public static AccountPrincipal fromClaims(
            String accountId,
            String email,
            List<String> roles) {
        return new AccountPrincipal(
                UUID.fromString(accountId),
                email,
                null,
                AccountStatus.ACTIVE,
                roles
        );
    }

    private AccountPrincipal(
            UUID accountId,
            String email,
            @Nullable String password,
            AccountStatus status,
            List<String> roles) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.status = status;
        this.authorities = roles
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    // ── UserDetails ──────────────────────────────────────────────────────────

    /** Returns the usernameOrEmail address. Spring Security uses this as the username. */
    @NonNull
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isEnabled() {
        return AccountStatus.ACTIVE.equals(status);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !AccountStatus.BLOCKED.equals(status);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @NonNull
    public UUID getAccountId() {
        return accountId;
    }
}