package dev.d76.authx.account.infrastructure.persistence;

import dev.d76.authx.account.domain.model.Account;
import dev.d76.authx.account.domain.port.out.AccountRepository;
import dev.d76.authx.account.domain.vo.AccountId;
import dev.d76.authx.account.domain.vo.Username;
import dev.d76.authx.kernel.vo.Email;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class JpaAccountRepository implements AccountRepository {

    private final SpringDataAccountRepository jpa;
    private final AccountMapper mapper;

    public JpaAccountRepository(SpringDataAccountRepository jpa, AccountMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Account save(Account account) {
        JpaAccount entity = mapper.toJpa(account);
        JpaAccount saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findById(AccountId id) {

        return jpa
                .findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(Email email) {

        return jpa
                .findByEmailEqualsIgnoreCase(email.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByUsername(Username username) {

        return jpa
                .findByUsernameEqualsIgnoreCase(username.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpa
                .existsByEmailEqualsIgnoreCase(email.value());
    }

    @Override
    public boolean existsByUsername(Username username) {
        return jpa
                .existsByUsernameEqualsIgnoreCase(username.value());
    }

    @Override
    public Optional<Account> findByEmailOrUsername(String identifier) {
        String normalized = identifier.strip().toLowerCase();
        return jpa
                .findByEmailOrUsername(normalized, normalized)
                .map(mapper::toDomain);
    }
}