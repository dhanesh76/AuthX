package dev.d76.authx.account.domain.port.out;


import dev.d76.authx.account.domain.vo.EncodedPassword;

public interface PasswordEncoder {
    EncodedPassword encode(String rawPassword);
    boolean matches(String rawPassword, EncodedPassword encodedPassword);
}