package d76.app.home;

import io.jsonwebtoken.Jwts;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Base64;

@RestController
@EnableMethodSecurity
public class HomeController {

//    @GetMapping("/")
//    String home() {
//        return System.getProperty("os.name");
//    }

    @GetMapping("/user/secured")
    @PreAuthorize("hasRole('USER')")
    String secured() {
        return "You are seeing this because you are authenticated";
    }


    @GetMapping("/admin/secured")
    @PreAuthorize("hasRole('ADMIN')")
    String adminSecured() {
        return "You are seeing this because you are admin";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/secret")
    String generateSecret() {
        Key key = Jwts.SIG.HS256.key().build();
        return Base64.getEncoder().encodeToString(key.getEncoded());

    }
}
