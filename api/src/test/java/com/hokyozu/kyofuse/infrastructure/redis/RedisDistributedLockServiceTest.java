package com.hokyozu.kyofuse.infrastructure.redis;

import com.hokyozu.kyofuse.shared.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisDistributedLockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisDistributedLockService lockService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void tryAcquireReturnsTrueWhenKeyAbsent() {
        when(valueOperations.setIfAbsent(eq("lock:test-resource"), anyString(), eq(Duration.ofSeconds(5))))
                .thenReturn(true);

        boolean acquired = lockService.tryAcquire("test-resource", "val-1", Duration.ofSeconds(5));

        assertThat(acquired).isTrue();
    }

    @Test
    void tryAcquireReturnsFalseWhenKeyAlreadyExists() {
        when(valueOperations.setIfAbsent(eq("lock:test-resource"), anyString(), eq(Duration.ofSeconds(5))))
                .thenReturn(false);

        boolean acquired = lockService.tryAcquire("test-resource", "val-2", Duration.ofSeconds(5));

        assertThat(acquired).isFalse();
    }

    @Test
    void releaseExecutesLuaScript() {
        when(redisTemplate.execute(any(), eq(List.of("lock:test-resource")), eq("val-1")))
                .thenReturn(1L);

        boolean released = lockService.release("test-resource", "val-1");

        assertThat(released).isTrue();
    }

    @Test
    void executeWithLockExecutesTaskAndReleasesLock() {
        when(valueOperations.setIfAbsent(eq("lock:task-resource"), anyString(), any(Duration.class)))
                .thenReturn(true);
        when(redisTemplate.execute(any(), eq(List.of("lock:task-resource")), anyString()))
                .thenReturn(1L);

        AtomicBoolean executed = new AtomicBoolean(false);
        String result = lockService.executeWithLock("task-resource", Duration.ofSeconds(5), () -> {
            executed.set(true);
            return "done";
        });

        assertThat(result).isEqualTo("done");
        assertThat(executed.get()).isTrue();
        verify(redisTemplate).execute(any(), eq(List.of("lock:task-resource")), anyString());
    }

    @Test
    void executeWithLockThrowsConflictExceptionWhenLockNotAcquired() {
        when(valueOperations.setIfAbsent(eq("lock:busy-resource"), anyString(), any(Duration.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> lockService.executeWithLock("busy-resource", Duration.ofSeconds(5), () -> "fail"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Outra operação idêntica está em andamento");
    }
}
