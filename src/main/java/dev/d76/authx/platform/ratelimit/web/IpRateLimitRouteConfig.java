package dev.d76.authx.platform.ratelimit.web;

import dev.d76.authx.platform.ratelimit.RateLimitPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class IpRateLimitRouteConfig {

    @Bean
    public Map<String, RateLimitPolicy> ipRateLimitRoutes() {
        return Map.of(
                "POST:/api/challenges",  RateLimitPolicy.OTP_BY_IP
                // future:
                // "POST:/api/auth/login", RateLimitPolicy.LOGIN_BY_IP
        );
    }
}
