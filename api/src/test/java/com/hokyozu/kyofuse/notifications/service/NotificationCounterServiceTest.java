package com.hokyozu.kyofuse.notifications.service;

import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.repository.NotificationRepository;
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
class NotificationCounterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private NotificationCounterService notificationCounterService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getUnreadCountReturnsValueFromRedisWhenPresent() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("notifications:unread:count:" + userId)).thenReturn("5");

        long count = notificationCounterService.getUnreadCount(userId);

        assertThat(count).isEqualTo(5L);
        verify(notificationRepository, never()).countByUserIdAndStatus(any(), any());
    }

    @Test
    void getUnreadCountFetchesFromDatabaseAndCachesWhenRedisMiss() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("notifications:unread:count:" + userId)).thenReturn(null);
        when(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD)).thenReturn(3L);

        long count = notificationCounterService.getUnreadCount(userId);

        assertThat(count).isEqualTo(3L);
        verify(valueOperations).set(eq("notifications:unread:count:" + userId), eq("3"), any(Duration.class));
    }

    @Test
    void incrementIncrementsRedisWhenKeyExists() {
        UUID userId = UUID.randomUUID();
        when(redisTemplate.hasKey("notifications:unread:count:" + userId)).thenReturn(true);

        notificationCounterService.increment(userId);

        verify(valueOperations).increment("notifications:unread:count:" + userId);
    }

    @Test
    void decrementDecrementsRedisWhenValueGreaterThanZero() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("notifications:unread:count:" + userId)).thenReturn("2");

        notificationCounterService.decrement(userId);

        verify(valueOperations).decrement("notifications:unread:count:" + userId);
    }

    @Test
    void decrementDoesNotDecrementWhenZero() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("notifications:unread:count:" + userId)).thenReturn("0");

        notificationCounterService.decrement(userId);

        verify(valueOperations, never()).decrement("notifications:unread:count:" + userId);
    }

    @Test
    void resetSetsZeroInRedis() {
        UUID userId = UUID.randomUUID();

        notificationCounterService.reset(userId);

        verify(valueOperations).set(eq("notifications:unread:count:" + userId), eq("0"), any(Duration.class));
    }
}
