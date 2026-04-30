package org.nembx.app.common.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.nembx.app.common.entity.dto.LimitResponse;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.common.service.RateLimitService;
import org.springframework.stereotype.Component;

/**
 * @author Lian
 */

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class LimitAspect {
    private final RateLimitService rateLimitService;

    @Before("@annotation(Limit)")
    public void rateLimit(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Limit annotation = methodSignature.getMethod().getAnnotation(Limit.class);
        String name = annotation.name();
        int limitCount = annotation.count();

        LimitResponse limit = rateLimitService.limit(name, limitCount, 1);
        if (!limit.lock()) {
            log.warn("API {} 未通过限流", name);
            throw new BusinessException(ErrorCode.OVER_RATE_LIMIT);
        }
        log.debug("API {} 通过限流，剩余: {}", name, limit.remaining());
    }
}
