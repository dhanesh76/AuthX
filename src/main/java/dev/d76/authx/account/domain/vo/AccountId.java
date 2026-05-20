package dev.d76.authx.account.domain.vo;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jmolecules.ddd.types.Identifier;

import java.util.Objects;
import java.util.UUID;

@ValueObject
public record AccountId(UUID value) implements Identifier {

    public AccountId {
        Objects.requireNonNull(value, "AccountId must not be null");
    }

    public AccountId() {
        this(UUID.randomUUID());
    }
}