package com.hokyozu.kyofuse.infrastructure.redis;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    /**
     * Expressão SpEL para a chave do lock.
     * Exemplo: "'team:invite:' + #inviteId" ou "'community:join:' + #communityId"
     */
    String key();

    /**
     * Tempo de expiração do lock em segundos (Lease Time).
     * Padrão: 5 segundos.
     */
    long leaseTimeSeconds() default 5;
}
