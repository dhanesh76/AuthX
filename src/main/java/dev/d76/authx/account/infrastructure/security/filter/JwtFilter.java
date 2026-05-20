package dev.d76.authx.account.infrastructure.security.filter;

import dev.d76.authx.account.application.port.out.TokenPort;
import dev.d76.authx.account.infrastructure.security.AccountPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.d76.spring.exception.BusinessException;
import dev.d76.spring.exception.autoconfigure.web.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenPort    tokenPort;
    private final ObjectMapper objectMapper;

    @Override
    @NullMarked
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // no token — pass through, Spring Security handles anonymous access
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // context already populated — another filter ran first, skip
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        String raw = header.substring(7).strip();

        try {
            TokenPort.AccessClaims claims = tokenPort.verifyAccessToken(raw);

            List<SimpleGrantedAuthority> authorities = claims.roles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            AccountPrincipal principal = AccountPrincipal.fromClaims(
                    claims.accountId(),
                    claims.email(),
                    claims.roles()
            );

            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    authorities
            );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (BusinessException ex) {
            var errorResponse = ApiErrorResponse
                    .builderFrom(ex.getErrorCode(), request)
                    .build();

            response.setStatus(errorResponse.httpStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }

        chain.doFilter(request, response);
    }
}