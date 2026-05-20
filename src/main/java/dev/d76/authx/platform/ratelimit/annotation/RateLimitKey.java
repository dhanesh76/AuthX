package dev.d76.authx.platform.ratelimit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// marks which method parameter is the rate limit key
// the aspect will call .toString() on it

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimitKey {
}
