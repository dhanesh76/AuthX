package dev.d76.authx.account.infrastructure.persistence;

import dev.d76.authx.account.domain.model.AccountStatus;
import dev.d76.authx.account.domain.model.IdentityProvider;
import dev.d76.authx.account.domain.model.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaAccount {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "encoded_password")
    private String encodedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "account_identity_providers",
            joinColumns = @JoinColumn(name = "account_id")
    )
    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Builder.Default
    private Set<IdentityProvider> identityProviders = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id")
    )
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}