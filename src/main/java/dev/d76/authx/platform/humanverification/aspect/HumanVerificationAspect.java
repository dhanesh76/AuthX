package dev.d76.authx.platform.humanverification.aspect;

import dev.d76.authx.kernel.exception.KernelErrorCode;
import dev.d76.authx.platform.humanverification.HumanVerificationRequest;
import dev.d76.authx.platform.humanverification.HumanVerifier;
import dev.d76.authx.platform.humanverification.annotation.RequiresHumanVerification;
import dev.d76.spring.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HumanVerificationAspect {
    private static final String VERIFICATION_HEADER = "X-Verification-Token";

    private final HumanVerifier humanVerifier;

    @Before("@annotation(requiresHumanVerification)")
    void enforce(RequiresHumanVerification requiresHumanVerification) {
        String action = requiresHumanVerification.action();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();

        Objects.requireNonNull(attributes);
        HttpServletRequest request = attributes.getRequest();

        String token = request.getHeader(VERIFICATION_HEADER);
        String clientIp = request.getRemoteAddr();

        if (token == null || token.isBlank()) {
            log.warn("Verification token missing — action={} clientIp={}", action, clientIp);
            throw new BusinessException(KernelErrorCode.VERIFICATION_TOKEN_MISSING);
        }

        HumanVerificationRequest verificationRequest = new HumanVerificationRequest(
                token,
                action,
                clientIp
        );

        boolean verified = humanVerifier.verify(verificationRequest);

        if (!verified) {
            log.warn("Human verification failed - action={}", action);
            throw new BusinessException(KernelErrorCode.HUMAN_VERIFICATION_FAILED);
        }
    }
}
