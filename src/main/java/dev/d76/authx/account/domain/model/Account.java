package dev.d76.authx.account.domain.model;

import dev.d76.authx.account.domain.exception.AccountErrorCode;
import dev.d76.authx.account.domain.port.out.PasswordEncoder;
import dev.d76.authx.account.domain.vo.AccountId;
import dev.d76.authx.account.domain.vo.EncodedPassword;
import dev.d76.authx.account.domain.vo.RawPassword;
import dev.d76.authx.account.domain.vo.Username;
import dev.d76.authx.kernel.vo.Email;
import dev.d76.spring.exception.BusinessException;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AggregateRoot
public class Account {

    @Identity
    private AccountId id;
    private Email email;
    private Username username;
    private EncodedPassword encodedPassword;
    private Set<Role> roles = new HashSet<>();
    private Set<IdentityProvider> identityProviders = new HashSet<>();
    private AccountStatus accountStatus;
    private Instant createdAt;

    private Account(Username username, Email email, EncodedPassword encodedPassword, Instant createdAt) {
        this.id = new AccountId();
        this.username = username;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.identityProviders.add(IdentityProvider.EMAIL);
        this.accountStatus = AccountStatus.VERIFICATION_PENDING;
        this.createdAt = createdAt;
        this.roles.add(Role.USER);
    }

    private Account() {
    }

    // ── Factories ────────────────────────────────────────────────────────────

    /**
     * Creates a credential-based account. Starts as {@code VERIFICATION_PENDING} —
     * the account is not usable until the usernameOrEmail is verified via OTP.
     */
    public static Account createPendingAccount(
            Username username, Email email,
            RawPassword rawPassword, PasswordEncoder encoder, Instant now) {
        return new Account(username, email, encoder.encode(rawPassword.value()), now);
    }

    /**
     * Creates a federated account. Starts as {@code ACTIVE} immediately —
     * the identity provider has already verified usernameOrEmail ownership.
     * No password is set; credential login is unavailable until one is established
     * via the password reset flow.
     */
    public static Account createFederateAccount(
            Username username, Email email,
            IdentityProvider provider, Instant now) {
        Account account = new Account();
        account.id = new AccountId();
        account.username = username;
        account.email = email;
        account.encodedPassword = null;
        account.identityProviders = new HashSet<>(Set.of(provider));
        account.accountStatus = AccountStatus.ACTIVE;
        account.roles = new HashSet<>(Set.of(Role.USER));
        account.createdAt = now;
        return account;
    }

    // ── Behaviour ────────────────────────────────────────────────────────────

    public void activate() {
        if (AccountStatus.ACTIVE == accountStatus) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_ALREADY_ACTIVE);
        }
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void block() {
        requireActiveAccount();
        this.accountStatus = AccountStatus.BLOCKED;
    }

    /**
     * Changes the account password.
     * <p>
     * Social-only accounts (no password set) may use this to establish a first
     * password — the same-password check is skipped because there is nothing to
     * compare against. This enables credential login alongside the federated provider
     * after the password reset flow completes.
     */
    public void changePassword(RawPassword newPassword, PasswordEncoder encoder) {
        requireActiveAccount();

        if (encodedPassword != null &&
                encoder.matches(newPassword.value(), encodedPassword)) {
            throw new BusinessException(AccountErrorCode.SAME_PASSWORD);
        }
        this.encodedPassword = encoder.encode(newPassword.value());
    }

    /**
     * Verifies the provided password against the stored hash.
     * <p>
     * {@code attempted} is accepted as a plain {@code String} rather than
     * {@code RawPassword} because the current password already exists in the system
     * and was validated at creation time. Re-applying format rules to an existing
     * credential would incorrectly reject passwords set before stricter rules existed.
     */
    public void verifyPassword(String attempted, PasswordEncoder encoder) {
        requireActiveAccount();

        if (encodedPassword == null) {
            // Social-only account — no password was ever set
            throw new BusinessException(AccountErrorCode.INVALID_CREDENTIALS);
        }

        if (!encoder.matches(attempted, encodedPassword)) {
            throw new BusinessException(AccountErrorCode.INVALID_CREDENTIALS);
        }
    }

    /** Pure query — does not enforce account lifecycle. */
    public boolean hasIdentityProvider(IdentityProvider provider) {
        return identityProviders.contains(provider);
    }

    /**
     * Associates a federated identity provider with this account.
     * <p>
     * If the account is {@code VERIFICATION_PENDING}, linking activates it
     * immediately. The provider's usernameOrEmail verification is treated as authoritative
     * proof of usernameOrEmail ownership — equivalent in trust to OTP verification.
     * This mirrors the behaviour of GitHub and other major identity systems.
     */
    public void linkProvider(IdentityProvider provider) {
        if (AccountStatus.BLOCKED.equals(accountStatus)) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_BLOCKED);
        }

        if (identityProviders.contains(provider)) {
            throw new BusinessException(AccountErrorCode.PROVIDER_ALREADY_LINKED);
        }

        identityProviders.add(provider);

        if (AccountStatus.VERIFICATION_PENDING.equals(accountStatus)) {
            this.accountStatus = AccountStatus.ACTIVE;
        }
    }

    void requireActiveAccount() {
        if (!AccountStatus.ACTIVE.equals(accountStatus)) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_ACTIVE);
        }
    }

    public void requireProviderLinked(IdentityProvider provider) {
        if (!identityProviders.contains(provider)) {
            throw new BusinessException(AccountErrorCode.PROVIDER_NOT_LINKED);
        }
    }

    // ── Snapshot ─────────────────────────────────────────────────────────────

    public record AccountSnapshot(
            UUID accountId, String email, String username, String encodedPassword,
            AccountStatus status, Set<IdentityProvider> identityProviders,
            Set<Role> roles, Instant createdAt) {
    }

    public AccountSnapshot snapshot() {
        return new AccountSnapshot(
                id.value(), email.value(), username.value(),
                encodedPassword != null ? encodedPassword.value() : null,
                accountStatus, Set.copyOf(identityProviders),
                Set.copyOf(roles), createdAt);
    }

    public static Account from(AccountSnapshot snap) {
        Account account = new Account();
        account.id = new AccountId(snap.accountId());
        account.email = new Email(snap.email());
        account.username = new Username(snap.username());
        account.encodedPassword = snap.encodedPassword() != null
                ? new EncodedPassword(snap.encodedPassword()) : null;
        account.accountStatus = snap.status();
        account.identityProviders = new HashSet<>(snap.identityProviders());
        account.roles = new HashSet<>(snap.roles());
        account.createdAt = snap.createdAt();
        return account;
    }
}