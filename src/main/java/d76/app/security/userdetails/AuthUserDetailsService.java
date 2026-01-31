package d76.app.security.userdetails;

import d76.app.security.principal.UserPrincipal;
import d76.app.user.entity.Users;
import d76.app.user.service.UserService;
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

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Users user = userService.loadUserByEmailOrUsername(usernameOrEmail);
        return UserPrincipal.fromUserEntity(user);
    }
}
