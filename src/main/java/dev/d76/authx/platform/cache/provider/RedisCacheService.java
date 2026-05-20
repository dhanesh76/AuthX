package dev.d76.authx.platform.cache.provider;

import dev.d76.authx.platform.cache.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);

    @Override
    public <T> void put(String key, T value, long ttl, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return Optional.empty();
        }

        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }

        try {
            return Optional.ofNullable(objectMapper.convertValue(value, type));
        } catch (Exception e) {
            log.error("Error deserializing value from key [{}] to type [{}]", key, type.getSimpleName(), e);

            return Optional.empty();
        }
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
    }
}