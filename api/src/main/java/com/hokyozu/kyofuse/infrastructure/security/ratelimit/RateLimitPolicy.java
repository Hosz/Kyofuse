package com.hokyozu.kyofuse.infrastructure.security.ratelimit;

import java.time.Duration;
import java.util.List;

/**
 * maxAttempts/window definem o bucket (quantas tentativas são toleradas dentro da
 * janela antes de bloquear); lockoutTiers define a escalada de bloqueio: a 1ª vez que o
 * bucket estoura usa lockoutTiers.get(0), a 2ª usa lockoutTiers.get(1), e assim por
 * diante até o último item da lista (teto).
 */
public record RateLimitPolicy(int maxAttempts, Duration window, List<Duration> lockoutTiers) {
}
