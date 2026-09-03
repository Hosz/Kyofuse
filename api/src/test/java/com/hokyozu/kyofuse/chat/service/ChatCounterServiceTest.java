package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.repository.MessageReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatCounterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private MessageReceiptRepository messageReceiptRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ChatCounterService chatCounterService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getTotalUnreadCountReturnsValueFromRedisWhenPresent() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("chat:unread:total:" + userId)).thenReturn("12");

        long count = chatCounterService.getTotalUnreadCount(userId);

        assertThat(count).isEqualTo(12L);
        verify(messageReceiptRepository, never()).countTotalUnreadByUserId(any());
    }

    @Test
    void getTotalUnreadCountFetchesFromDatabaseWhenRedisMiss() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("chat:unread:total:" + userId)).thenReturn(null);
        when(messageReceiptRepository.countTotalUnreadByUserId(userId)).thenReturn(7L);

        long count = chatCounterService.getTotalUnreadCount(userId);

        assertThat(count).isEqualTo(7L);
        verify(valueOperations).set(eq("chat:unread:total:" + userId), eq("7"), any(Duration.class));
    }

    @Test
    void incrementIncrementsRedisWhenKeyExists() {
        UUID userId = UUID.randomUUID();
        when(redisTemplate.hasKey("chat:unread:total:" + userId)).thenReturn(true);

        chatCounterService.increment(userId);

        verify(valueOperations).increment("chat:unread:total:" + userId);
    }

    @Test
    void decrementByDecreasesRedisValue() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("chat:unread:total:" + userId)).thenReturn("10");

        chatCounterService.decrementBy(userId, 4L);

        verify(valueOperations).set(eq("chat:unread:total:" + userId), eq("6"), any(Duration.class));
    }

    @Test
    void decrementByClampsToZero() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("chat:unread:total:" + userId)).thenReturn("2");

        chatCounterService.decrementBy(userId, 5L);

        verify(valueOperations).set(eq("chat:unread:total:" + userId), eq("0"), any(Duration.class));
    }

    @Test
    void resetDeletesKey() {
        UUID userId = UUID.randomUUID();

        chatCounterService.reset(userId);

        verify(redisTemplate).delete("chat:unread:total:" + userId);
    }
}
