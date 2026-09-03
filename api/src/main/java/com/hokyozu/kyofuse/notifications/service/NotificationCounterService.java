package com.hokyozu.kyofuse.notifications.service;

import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCounterService {

    private final StringRedisTemplate redisTemplate;
    private final NotificationRepository notificationRepository;

    public static final String UNREAD_KEY_PREFIX = "notifications:unread:count:";
    private static final Duration COUNTER_TTL = Duration.ofDays(7);

    public long getUnreadCount(UUID userId) {
        String key = UNREAD_KEY_PREFIX + userId;
        String val = redisTemplate.opsForValue().get(key);

        if (val != null) {
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException ignored) {}
        }

        long count = notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        redisTemplate.opsForValue().set(key, String.valueOf(count), COUNTER_TTL);
        return count;
    }

    public void increment(UUID userId) {
        String key = UNREAD_KEY_PREFIX + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key);
        } else {
            getUnreadCount(userId);
        }
    }

    public void decrement(UUID userId) {
        String key = UNREAD_KEY_PREFIX + userId;
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            try {
                long current = Long.parseLong(val);
                if (current > 0) {
                    redisTemplate.opsForValue().decrement(key);
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    public void reset(UUID userId) {
        String key = UNREAD_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, "0", COUNTER_TTL);
    }
}
