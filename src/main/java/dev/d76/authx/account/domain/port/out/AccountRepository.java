package dev.d76.authx.account.domain.port.out;

import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.vo.AccountId;
import dev.d76.authx.account.domain.vo.Username;
import dev.d76.authx.kernel.vo.Email;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findById(AccountId id);

    Optional<Account> findByEmail(Email email);

    Optional<Account> findByUsername(Username username);

    boolean existsByEmail(Email email);

    boolean existsByUsername(Username username);

    Optional<Account> findByEmailOrUsername(String usernameOrEmail);
}