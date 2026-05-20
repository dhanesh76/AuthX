package dev.d76.authx.platform.ratelimit.web;

import dev.d76.authx.platform.ratelimit.exception.RateLimitExceededException;
import dev.d76.spring.exception.autoconfigure.web.ApiErrorResponseCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.time.Instant;

@Configuration
public class RateLimitResponseConfig {

    @Bean
    public ApiErrorResponseCustomizer rateLimitHeaderCustomizer() {
        return (builder, ex, request, response) -> {
            if (!(ex instanceof RateLimitExceededException rl)) return;

            long retryAt = Instant.now()
                    .plusSeconds(rl.retryAfterSeconds())
                    .getEpochSecond();

            response.setHeader(HttpHeaders.RETRY_AFTER,
                    String.valueOf(rl.retryAfterSeconds()));

            response.setHeader("X-Retry-At", String.valueOf(retryAt));
        };
    }
}