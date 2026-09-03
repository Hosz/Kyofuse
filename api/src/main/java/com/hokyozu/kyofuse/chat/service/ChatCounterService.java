package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.repository.MessageReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatCounterService {

    private final StringRedisTemplate redisTemplate;
    private final MessageReceiptRepository messageReceiptRepository;

    public static final String CHAT_TOTAL_UNREAD_PREFIX = "chat:unread:total:";
    private static final Duration COUNTER_TTL = Duration.ofDays(7);

    public long getTotalUnreadCount(UUID userId) {
        String key = CHAT_TOTAL_UNREAD_PREFIX + userId;
        String val = redisTemplate.opsForValue().get(key);

        if (val != null) {
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException ignored) {}
        }

        long count = messageReceiptRepository.countTotalUnreadByUserId(userId);
        redisTemplate.opsForValue().set(key, String.valueOf(count), COUNTER_TTL);
        return count;
    }

    public void increment(UUID userId) {
        String key = CHAT_TOTAL_UNREAD_PREFIX + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key);
        } else {
            getTotalUnreadCount(userId);
        }
    }

    public void decrementBy(UUID userId, long amount) {
        if (amount <= 0) return;

        String key = CHAT_TOTAL_UNREAD_PREFIX + userId;
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            try {
                long current = Long.parseLong(val);
                long updated = Math.max(0, current - amount);
                redisTemplate.opsForValue().set(key, String.valueOf(updated), COUNTER_TTL);
            } catch (NumberFormatException ignored) {}
        } else {
            getTotalUnreadCount(userId);
        }
    }

    public void reset(UUID userId) {
        String key = CHAT_TOTAL_UNREAD_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
