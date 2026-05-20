package dev.d76.authx.account.infrastructure.persistence;

import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAccountRepository extends JpaRepository<JpaAccount, UUID> {

    Optional<JpaAccount> findByEmailEqualsIgnoreCase(String email);

    Optional<JpaAccount> findByUsernameEqualsIgnoreCase(String username);

    boolean existsByUsernameEqualsIgnoreCase(String username);

    boolean existsByEmailEqualsIgnoreCase(String email);

    Optional<JpaAccount> findByEmailOrUsername(String email, String username);
}
