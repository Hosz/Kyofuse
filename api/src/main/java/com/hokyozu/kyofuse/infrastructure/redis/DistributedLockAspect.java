package com.hokyozu.kyofuse.infrastructure.redis;

import com.hokyozu.kyofuse.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedisDistributedLockService lockService;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(distributedLock)")
    public Object handleLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String key = resolveSpelKey(joinPoint, distributedLock.key());
        Duration leaseTime = Duration.ofSeconds(distributedLock.leaseTimeSeconds());

        try {
            return lockService.executeWithLock(key, leaseTime, () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeWrapperException(e);
                }
            });
        } catch (RuntimeWrapperException e) {
            throw e.getActualCause();
        }
    }

    private String resolveSpelKey(ProceedingJoinPoint joinPoint, String spelExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = nameDiscoverer.getParameterNames(method);

        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        try {
            Expression expression = parser.parseExpression(spelExpression);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : spelExpression;
        } catch (Exception e) {
            log.warn("Falha ao avaliar SpEL '{}': {}", spelExpression, e.getMessage());
            return spelExpression;
        }
    }

    public static class RuntimeWrapperException extends RuntimeException {
        private final Throwable actualCause;

        public RuntimeWrapperException(Throwable actualCause) {
            super(actualCause);
            this.actualCause = actualCause;
        }

        public Throwable getActualCause() {
            return actualCause;
        }
    }
}
