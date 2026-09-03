package com.hokyozu.kyofuse.infrastructure.redis;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributedLockAspectTest {

    @Mock
    private RedisDistributedLockService lockService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private DistributedLockAspect aspect;

    public void dummyMethod(UUID teamId, UUID userId) {}

    @Test
    void handleLockEvaluatesSpelKeyAndDelegatesToLockService() throws Throwable {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Method method = getClass().getMethod("dummyMethod", UUID.class, UUID.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{teamId, userId});

        DistributedLock lockAnnotation = createDistributedLock("'team:join:' + #teamId", 5);

        when(lockService.executeWithLock(eq("team:join:" + teamId), eq(Duration.ofSeconds(5)), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(2);
                    return supplier.get();
                });
        when(joinPoint.proceed()).thenReturn("success");

        Object result = aspect.handleLock(joinPoint, lockAnnotation);

        assertThat(result).isEqualTo("success");
    }

    private DistributedLock createDistributedLock(String key, long leaseTimeSeconds) {
        return new DistributedLock() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DistributedLock.class;
            }

            @Override
            public String key() {
                return key;
            }

            @Override
            public long leaseTimeSeconds() {
                return leaseTimeSeconds;
            }
        };
    }
}
