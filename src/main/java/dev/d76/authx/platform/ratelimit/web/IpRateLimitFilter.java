package dev.d76.authx.platform.ratelimit.web;

import dev.d76.authx.platform.ratelimit.RateLimitPolicy;
import dev.d76.authx.platform.ratelimit.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@NullMarked
@RequiredArgsConstructor
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final Map<String, RateLimitPolicy> rateLimitPolicies;
    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String routeKey = request.getMethod() + ":" + request.getRequestURI();

        Optional
                .ofNullable(rateLimitPolicies.get(routeKey))
                .ifPresent(policy -> rateLimitService
                        .consume(resolveIp(request), policy));

        filterChain.doFilter(request, response);
    }

    private String resolveIp(HttpServletRequest request) {
        // We intentionally use request.getRemoteAddr() as the source of truth.
        //
        // WHY:
        // - In our current deployment (direct VPS / no reverse proxy),
        //   remoteAddr is the actual client IP.
        //
        // - Headers like "X-Forwarded-For" are USER-CONTROLLED unless a trusted
        //   proxy (NGINX / Load Balancer / CDN) is in front of the app.
        //   Blindly trusting it would allow IP spoofing.
        //
        // WHEN TO CHANGE THIS:
        // - If we deploy behind a trusted proxy (NGINX, Cloudflare, etc.),
        //   then:
        //     1. Ensure the proxy sets X-Forwarded-For correctly
        //     2. Only trust it if request.getRemoteAddr() belongs to that proxy
        //
        // Until then → use remoteAddr only (safe default)
        return request.getRemoteAddr();
    }
}
