package com.hokyozu.kyofuse.infrastructure.ratelimit;

import com.hokyozu.kyofuse.shared.exception.TooManyAttemptsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RedisRateLimiterService rateLimiterService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void enforceRateLimitProceedsWhenAllowed() throws Throwable {
        RateLimit rateLimit = createRateLimit("test_key", 5, 60, RateLimitType.IP);
        request.setRemoteAddr("192.168.1.1");

        when(rateLimiterService.isAllowed("test_key:ip:192.168.1.1", 5, 60))
                .thenReturn(new RedisRateLimiterService.RateLimitResult(true, 1, 5, 0));
        when(joinPoint.proceed()).thenReturn("success");

        Object result = rateLimitAspect.enforceRateLimit(joinPoint, rateLimit);

        assertThat(result).isEqualTo("success");
    }

    @Test
    void enforceRateLimitThrowsTooManyAttemptsExceptionWhenDenied() throws Throwable {
        RateLimit rateLimit = createRateLimit("test_key", 5, 60, RateLimitType.IP);
        request.setRemoteAddr("192.168.1.1");

        when(rateLimiterService.isAllowed("test_key:ip:192.168.1.1", 5, 60))
                .thenReturn(new RedisRateLimiterService.RateLimitResult(false, 5, 5, 30));

        assertThatThrownBy(() -> rateLimitAspect.enforceRateLimit(joinPoint, rateLimit))
                .isInstanceOf(TooManyAttemptsException.class)
                .hasMessageContaining("30 segundos");

        assertThat(response.getHeader("Retry-After")).isEqualTo("30");
        verify(joinPoint, never()).proceed();
    }

    @Test
    void enforceRateLimitUsesUserIdWhenAuthenticated() throws Throwable {
        UUID userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        "credentials",
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        RateLimit rateLimit = createRateLimit("user_key", 10, 60, RateLimitType.USER_ID);

        when(rateLimiterService.isAllowed("user_key:user:" + userId, 10, 60))
                .thenReturn(new RedisRateLimiterService.RateLimitResult(true, 1, 10, 0));
        when(joinPoint.proceed()).thenReturn("user-success");

        Object result = rateLimitAspect.enforceRateLimit(joinPoint, rateLimit);

        assertThat(result).isEqualTo("user-success");
    }

    private RateLimit createRateLimit(String key, long limit, long period, RateLimitType type) {
        return new RateLimit() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return RateLimit.class;
            }

            @Override
            public String key() {
                return key;
            }

            @Override
            public long limit() {
                return limit;
            }

            @Override
            public long period() {
                return period;
            }

            @Override
            public RateLimitType type() {
                return type;
            }
        };
    }
}
