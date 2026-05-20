package dev.d76.authx.account.domain.model;

public enum Role {
    USER,
    ADMIN;
    public String toAuthority(){
        return "ROLE_" + this.name();
    }
}
