package d76.app.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import d76.app.auth.model.IdentityProvider;
import d76.app.core.exception.ApiErrorResponse;
import d76.app.core.exception.BusinessException;
import d76.app.security.principal.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
@NullMarked
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ") &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                String token = header.substring("Bearer ".length()).trim();

                var claims = jwtService.extractClaims(token);

                var userId = Long.parseLong(claims.getSubject());
                var email = claims.get("email", String.class);
                var identityProvider = IdentityProvider.valueOf(
                        claims.get("identityProvider", String.class)
                );

                var rolesClaim = claims.get("roles");
                List<String> roles = rolesClaim instanceof List<?> rawList ?
                        rawList.stream()
                                .filter(String.class::isInstance)
                                .map(String.class::cast)
                                .toList()
                        : List.of();

                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                var userPrincipal = UserPrincipal.fromJwt(userId, email, identityProvider, authorities);
                var authToken = UsernamePasswordAuthenticationToken.authenticated(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);

            }
            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            var errorCode = e.getErrorCode();

            response.setStatus(errorCode.getStatus().value());
            response.setContentType("application/json");

            var errorResponse = ApiErrorResponse
                    .builder()
                    .errorCode(errorCode.getCode())
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .path(request.getRequestURI())
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
