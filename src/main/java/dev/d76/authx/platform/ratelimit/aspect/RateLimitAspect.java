package dev.d76.authx.platform.ratelimit.aspect;

import dev.d76.authx.platform.ratelimit.RateLimitService;
import dev.d76.authx.platform.ratelimit.annotation.RateLimit;
import dev.d76.authx.platform.ratelimit.annotation.RateLimitKey;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;

    @Before("@annotation(rateLimit)")
    void applyRateLimit(JoinPoint jointPoint, RateLimit rateLimit) {
        rateLimitService.consume(
                resolveKey(jointPoint),
                rateLimit.policy()
        );
    }

    /**
     * Resolves the rate limit key from the method parameter annotated with {@code @RateLimitKey}.
     * The parameter's {@code toString()} value is used as the key — callers must ensure
     * the annotated parameter produces a meaningful, stable string (e.g. usernameOrEmail, IP address).
     */
    private String resolveKey(JoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RateLimitKey.class)) {
                return args[i].toString();
            }
        }

        throw new IllegalStateException(
                "@RateLimit method must have one parameter annotated with @RateLimitKey: "
                        + signature.getMethod().getName()
        );
    }
}