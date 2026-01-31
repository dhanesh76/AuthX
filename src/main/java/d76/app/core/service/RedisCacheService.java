package d76.app.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> void put(String key, T value, long ttl, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        var value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(type.cast(value));
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
    }
}
