package com.hokyozu.kyofuse.infrastructure.ratelimit;

import com.hokyozu.kyofuse.shared.exception.TooManyAttemptsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisRateLimiterService rateLimiterService;

    @Around("@annotation(rateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        String clientIdentifier = resolveIdentifier(request, rateLimit.type());
        String key = rateLimit.key() + ":" + clientIdentifier;

        RedisRateLimiterService.RateLimitResult result = rateLimiterService.isAllowed(
                key,
                rateLimit.limit(),
                rateLimit.period()
        );

        if (!result.allowed()) {
            if (response != null) {
                response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            }
            throw new TooManyAttemptsException(
                    "Limite de requisições excedido. Tente novamente em " + result.retryAfterSeconds() + " segundos.",
                    result.retryAfterSeconds()
            );
        }

        return joinPoint.proceed();
    }

    private String resolveIdentifier(HttpServletRequest request, RateLimitType type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName()
                : null;

        String clientIp = extractClientIp(request);

        return switch (type) {
            case USER_ID -> userId != null ? "user:" + userId : "ip:" + clientIp;
            case IP -> "ip:" + clientIp;
            case USER_OR_IP -> userId != null ? "user:" + userId : "ip:" + clientIp;
        };
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}
