package dev.d76.authx.platform.ratelimit;

import dev.d76.authx.platform.ratelimit.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ProxyManager<byte[]> proxyManager;

    /**
     * Consumes one token from the bucket identified by {@code key} and {@code policy}.
     * The bucket is created in Redis on first call and reused on subsequent calls —
     * atomic, distributed, and cluster-safe via Bucket4j + Lettuce.
     *
     * @param key    the rate limit subject — IP address, usernameOrEmail, user ID, etc.
     * @param policy the bucket configuration — capacity and refill window
     * @throws RateLimitExceededException if the bucket is empty
     */
    public void consume(String key, RateLimitPolicy policy) {

        byte[] bucketKey = ("rl:" + key).getBytes();

        Bucket bucket = proxyManager
                .builder()
                .build(bucketKey, policy::configuration);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long retryAfterSeconds = TimeUnit.NANOSECONDS
                    .toSeconds(probe.getNanosToWaitForRefill());

            throw new RateLimitExceededException(retryAfterSeconds);
        }
    }
}