package com.hokyozu.kyofuse.presence.service;

import com.hokyozu.kyofuse.presence.dto.response.PresenceResponse;
import com.hokyozu.kyofuse.presence.enums.PresenceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public static final String PRESENCE_USER_PREFIX = "presence:user:";
    public static final String PRESENCE_SESSIONS_PREFIX = "presence:sessions:";
    public static final String PRESENCE_LAST_SEEN_PREFIX = "presence:last_seen:";
    public static final Duration HEARTBEAT_TTL = Duration.ofSeconds(60);

    public void registerConnect(UUID userId, String sessionId) {
        String userKey = PRESENCE_USER_PREFIX + userId;
        String sessionsKey = PRESENCE_SESSIONS_PREFIX + userId;
        String lastSeenKey = PRESENCE_LAST_SEEN_PREFIX + userId;

        Long sessionCount = redisTemplate.opsForSet().add(sessionsKey, sessionId);
        redisTemplate.expire(sessionsKey, Duration.ofDays(1));

        redisTemplate.opsForValue().set(userKey, PresenceStatus.ONLINE.name(), HEARTBEAT_TTL);
        redisTemplate.opsForValue().set(lastSeenKey, String.valueOf(Instant.now().toEpochMilli()));

        if (sessionCount != null && sessionCount > 0) {
            Long totalSessions = redisTemplate.opsForSet().size(sessionsKey);
            if (totalSessions != null && totalSessions == 1L) {
                broadcastPresenceChange(userId, PresenceStatus.ONLINE, Instant.now());
            }
        }
    }

    public void registerDisconnect(UUID userId, String sessionId) {
        String sessionsKey = PRESENCE_SESSIONS_PREFIX + userId;
        String userKey = PRESENCE_USER_PREFIX + userId;
        String lastSeenKey = PRESENCE_LAST_SEEN_PREFIX + userId;

        redisTemplate.opsForSet().remove(sessionsKey, sessionId);
        Long remainingSessions = redisTemplate.opsForSet().size(sessionsKey);

        if (remainingSessions == null || remainingSessions == 0L) {
            redisTemplate.delete(userKey);
            redisTemplate.delete(sessionsKey);
            Instant now = Instant.now();
            redisTemplate.opsForValue().set(lastSeenKey, String.valueOf(now.toEpochMilli()));

            broadcastPresenceChange(userId, PresenceStatus.OFFLINE, now);
        }
    }

    public void heartbeat(UUID userId) {
        String userKey = PRESENCE_USER_PREFIX + userId;
        String lastSeenKey = PRESENCE_LAST_SEEN_PREFIX + userId;

        redisTemplate.opsForValue().set(userKey, PresenceStatus.ONLINE.name(), HEARTBEAT_TTL);
        redisTemplate.opsForValue().set(lastSeenKey, String.valueOf(Instant.now().toEpochMilli()));
    }

    public PresenceResponse getPresence(UUID userId) {
        String userKey = PRESENCE_USER_PREFIX + userId;
        String lastSeenKey = PRESENCE_LAST_SEEN_PREFIX + userId;

        boolean isOnline = Boolean.TRUE.equals(redisTemplate.hasKey(userKey));
        String lastSeenVal = redisTemplate.opsForValue().get(lastSeenKey);

        Instant lastSeen = lastSeenVal != null
                ? Instant.ofEpochMilli(Long.parseLong(lastSeenVal))
                : null;

        return new PresenceResponse(
                userId,
                isOnline ? PresenceStatus.ONLINE : PresenceStatus.OFFLINE,
                lastSeen
        );
    }

    public Map<UUID, PresenceResponse> getPresenceBatch(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<String> userKeys = userIds.stream().map(id -> PRESENCE_USER_PREFIX + id).toList();
        List<String> lastSeenKeys = userIds.stream().map(id -> PRESENCE_LAST_SEEN_PREFIX + id).toList();

        List<String> onlineResults = redisTemplate.opsForValue().multiGet(userKeys);
        List<String> lastSeenResults = redisTemplate.opsForValue().multiGet(lastSeenKeys);

        Map<UUID, PresenceResponse> responseMap = new LinkedHashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            UUID userId = userIds.get(i);
            String onlineVal = (onlineResults != null && i < onlineResults.size()) ? onlineResults.get(i) : null;
            String lastSeenVal = (lastSeenResults != null && i < lastSeenResults.size()) ? lastSeenResults.get(i) : null;

            boolean isOnline = onlineVal != null;
            Instant lastSeen = lastSeenVal != null
                    ? Instant.ofEpochMilli(Long.parseLong(lastSeenVal))
                    : null;

            responseMap.put(userId, new PresenceResponse(
                    userId,
                    isOnline ? PresenceStatus.ONLINE : PresenceStatus.OFFLINE,
                    lastSeen
            ));
        }

        return responseMap;
    }

    private void broadcastPresenceChange(UUID userId, PresenceStatus status, Instant timestamp) {
        PresenceResponse payload = new PresenceResponse(userId, status, timestamp);
        try {
            messagingTemplate.convertAndSend("/topic/presence", payload);
        } catch (Exception e) {
            log.warn("Failed to broadcast presence update for user {}: {}", userId, e.getMessage());
        }
    }
}
