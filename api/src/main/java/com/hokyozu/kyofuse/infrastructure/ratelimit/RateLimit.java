package com.hokyozu.kyofuse.infrastructure.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    String key() default "";
    long limit() default 10;
    long period() default 60; // tempo em segundos
    RateLimitType type() default RateLimitType.USER_OR_IP;
}
