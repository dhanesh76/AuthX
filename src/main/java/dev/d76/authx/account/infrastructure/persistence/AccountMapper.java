package dev.d76.authx.account.infrastructure.persistence;

import dev.d76.authx.account.domain.model.Account;
import org.jmolecules.ddd.annotation.Factory;
import org.springframework.stereotype.Component;

@Component
@Factory
public class AccountMapper {
    public JpaAccount toJpa(Account account) {
        Account.AccountSnapshot snap = account.snapshot();

        return JpaAccount.builder()
                .id(snap.accountId())
                .username(snap.username())
                .email(snap.email())
                .encodedPassword(snap.encodedPassword())
                .status(snap.status())
                .identityProviders(snap.identityProviders())
                .roles(snap.roles())
                .createdAt(snap.createdAt())
                .build();
    }

    public Account toDomain(JpaAccount entity) {
       Account.AccountSnapshot snap = new Account.AccountSnapshot(
                entity.getId(),
                entity.getEmail(),
                entity.getUsername(),
                entity.getEncodedPassword(),
                entity.getStatus(),
                entity.getIdentityProviders(),
                entity.getRoles(),
                entity.getCreatedAt()
        );
        return Account.from(snap);
    }
}