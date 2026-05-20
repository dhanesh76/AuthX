package dev.d76.authx.account.infrastructure.password;

import dev.d76.authx.account.domain.port.out.PasswordEncoder;
import dev.d76.authx.account.domain.vo.EncodedPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BcryptPasswordEncoderAdapter implements PasswordEncoder {

    private final BCryptPasswordEncoder bCryptEncoder;

    @Override
    public EncodedPassword encode(String rawPassword) {
        return new EncodedPassword(bCryptEncoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, EncodedPassword encodedPassword) {
        return bCryptEncoder.matches(rawPassword, encodedPassword.value());
    }
}