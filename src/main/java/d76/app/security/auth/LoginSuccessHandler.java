    package d76.app.security.auth;

    import d76.app.auth.dto.LoginSuccess;
    import d76.app.security.jwt.JwtService;
    import d76.app.security.principal.UserPrincipal;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import lombok.RequiredArgsConstructor;
    import org.jspecify.annotations.NullMarked;
    import org.springframework.http.HttpStatus;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
    import org.springframework.stereotype.Component;
    import tools.jackson.databind.ObjectMapper;

    import java.io.IOException;
    import java.time.Instant;

    @NullMarked
    @Component
    @RequiredArgsConstructor
    public class LoginSuccessHandler implements AuthenticationSuccessHandler {

        private final JwtService jwtService;
        private final ObjectMapper objectMapper;

        @Override
        public void onAuthenticationSuccess(
                HttpServletRequest request,
                HttpServletResponse response,
                Authentication authentication) throws IOException {

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            if (principal == null) throw new IllegalStateException("Unsupported principal type");

            String username = principal.getUsername();
            String provider = principal.getIdentityProvider().name();

            var token = jwtService.generateAccessToken(principal);

            var loginResponse = LoginSuccess.builder()
                    .status("LOGIN_SUCCESS")
                    .username(username)
                    .accessToken(token)
                    .identityProvider(provider)
                    .issuedAt(Instant.now())
                    .build();

            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), loginResponse);
        }
    }
