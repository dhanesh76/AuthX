package d76.app.security.userdetails;

import d76.app.core.exception.BusinessException;
import d76.app.user.entity.Users;
import d76.app.user.exception.UserErrorCode;
import d76.app.user.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@NullMarked
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Users user = usersRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, "No user exists with: " + usernameOrEmail));

        return new UserPrincipal(user);
    }
}
