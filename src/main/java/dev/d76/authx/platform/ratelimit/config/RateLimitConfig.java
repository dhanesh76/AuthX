package dev.d76.authx.platform.ratelimit.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final LettuceConnectionFactory lettuceConnectionFactory;

    @Bean
    ProxyManager<byte[]> proxyManager() {
        RedisClient client = (RedisClient) lettuceConnectionFactory.getNativeClient();

        ClientSideConfig config = ClientSideConfig
                .getDefault()
                .withExpirationAfterWriteStrategy(
                        ExpirationAfterWriteStrategy
                                .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10))
                );

        return LettuceBasedProxyManager
                .builderFor(Objects.requireNonNull(client))
                .withClientSideConfig(config)
                .build();
    }
}