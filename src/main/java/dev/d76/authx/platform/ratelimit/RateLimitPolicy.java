package dev.d76.authx.platform.ratelimit;

import io.github.bucket4j.BucketConfiguration;

import java.time.Duration;

public enum RateLimitPolicy {

    OTP_BY_EMAIL(
            BucketConfiguration.builder()
                    .addLimit(limit -> limit
                            .capacity(1)
                            .refillIntervally(1, Duration.ofSeconds(30)))
                    .addLimit(limit -> limit
                            .capacity(5)
                            .refillIntervally(5, Duration.ofMinutes(5)))
                    .build()
    ),

    OTP_BY_IP(
            BucketConfiguration.builder()
                    .addLimit(limit -> limit
                            .capacity(3)
                            .refillIntervally(3, Duration.ofSeconds(30)))
                    .addLimit(limit -> limit
                            .capacity(20)
                            .refillIntervally(20, Duration.ofMinutes(5)))
                    .build()
    );

    private final BucketConfiguration configuration;

    RateLimitPolicy(BucketConfiguration configuration) {
        this.configuration = configuration;
    }

    public BucketConfiguration configuration() {
        return configuration;
    }
}