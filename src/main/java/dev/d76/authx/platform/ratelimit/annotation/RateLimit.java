package dev.d76.authx.platform.ratelimit.annotation;

import dev.d76.authx.platform.ratelimit.RateLimitPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    RateLimitPolicy policy();
}
