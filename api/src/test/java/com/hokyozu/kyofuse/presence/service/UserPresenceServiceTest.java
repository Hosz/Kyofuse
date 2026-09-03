package com.hokyozu.kyofuse.presence.service;

import com.hokyozu.kyofuse.presence.dto.response.PresenceResponse;
import com.hokyozu.kyofuse.presence.enums.PresenceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPresenceServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserPresenceService userPresenceService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void registerConnectAddsSessionAndBroadcastsOnlineWhenFirstSession() {
        UUID userId = UUID.randomUUID();
        String sessionId = "sess-1";

        when(setOperations.add("presence:sessions:" + userId, sessionId)).thenReturn(1L);
        when(setOperations.size("presence:sessions:" + userId)).thenReturn(1L);

        userPresenceService.registerConnect(userId, sessionId);

        verify(setOperations).add("presence:sessions:" + userId, sessionId);
        verify(redisTemplate).expire("presence:sessions:" + userId, Duration.ofDays(1));
        verify(valueOperations).set(eq("presence:user:" + userId), eq(PresenceStatus.ONLINE.name()), eq(Duration.ofSeconds(60)));
        verify(valueOperations).set(eq("presence:last_seen:" + userId), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/presence"), any(PresenceResponse.class));
    }

    @Test
    void registerConnectDoesNotBroadcastWhenAlreadyHasActiveSessions() {
        UUID userId = UUID.randomUUID();
        String sessionId = "sess-2";

        when(setOperations.add("presence:sessions:" + userId, sessionId)).thenReturn(1L);
        when(setOperations.size("presence:sessions:" + userId)).thenReturn(2L);

        userPresenceService.registerConnect(userId, sessionId);

        verify(setOperations).add("presence:sessions:" + userId, sessionId);
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/presence"), any(PresenceResponse.class));
    }

    @Test
    void registerDisconnectRemovesSessionAndBroadcastsOfflineWhenLastSessionClosed() {
        UUID userId = UUID.randomUUID();
        String sessionId = "sess-1";

        when(setOperations.remove("presence:sessions:" + userId, sessionId)).thenReturn(1L);
        when(setOperations.size("presence:sessions:" + userId)).thenReturn(0L);

        userPresenceService.registerDisconnect(userId, sessionId);

        verify(setOperations).remove("presence:sessions:" + userId, sessionId);
        verify(redisTemplate).delete("presence:user:" + userId);
        verify(redisTemplate).delete("presence:sessions:" + userId);
        verify(valueOperations).set(eq("presence:last_seen:" + userId), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/presence"), any(PresenceResponse.class));
    }

    @Test
    void registerDisconnectKeepsOnlineWhenOtherSessionsRemain() {
        UUID userId = UUID.randomUUID();
        String sessionId = "sess-1";

        when(setOperations.remove("presence:sessions:" + userId, sessionId)).thenReturn(1L);
        when(setOperations.size("presence:sessions:" + userId)).thenReturn(1L);

        userPresenceService.registerDisconnect(userId, sessionId);

        verify(redisTemplate, never()).delete("presence:user:" + userId);
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/presence"), any(PresenceResponse.class));
    }

    @Test
    void heartbeatRefreshesUserTtlAndLastSeen() {
        UUID userId = UUID.randomUUID();

        userPresenceService.heartbeat(userId);

        verify(valueOperations).set(eq("presence:user:" + userId), eq(PresenceStatus.ONLINE.name()), eq(Duration.ofSeconds(60)));
        verify(valueOperations).set(eq("presence:last_seen:" + userId), anyString());
    }

    @Test
    void getPresenceReturnsOnlineStatusWhenKeyExists() {
        UUID userId = UUID.randomUUID();
        long nowMillis = Instant.now().toEpochMilli();

        when(redisTemplate.hasKey("presence:user:" + userId)).thenReturn(true);
        when(valueOperations.get("presence:last_seen:" + userId)).thenReturn(String.valueOf(nowMillis));

        PresenceResponse response = userPresenceService.getPresence(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo(PresenceStatus.ONLINE);
        assertThat(response.lastSeen()).isEqualTo(Instant.ofEpochMilli(nowMillis));
    }

    @Test
    void getPresenceReturnsOfflineWhenKeyDoesNotExist() {
        UUID userId = UUID.randomUUID();
        long nowMillis = Instant.now().minusSeconds(300).toEpochMilli();

        when(redisTemplate.hasKey("presence:user:" + userId)).thenReturn(false);
        when(valueOperations.get("presence:last_seen:" + userId)).thenReturn(String.valueOf(nowMillis));

        PresenceResponse response = userPresenceService.getPresence(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo(PresenceStatus.OFFLINE);
        assertThat(response.lastSeen()).isEqualTo(Instant.ofEpochMilli(nowMillis));
    }

    @Test
    void getPresenceBatchReturnsMapUsingMultiGet() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        long nowMillis = Instant.now().toEpochMilli();

        when(valueOperations.multiGet(List.of("presence:user:" + user1, "presence:user:" + user2)))
                .thenReturn(java.util.Arrays.asList("ONLINE", null));
        when(valueOperations.multiGet(List.of("presence:last_seen:" + user1, "presence:last_seen:" + user2)))
                .thenReturn(List.of(String.valueOf(nowMillis), String.valueOf(nowMillis - 5000)));

        Map<UUID, PresenceResponse> result = userPresenceService.getPresenceBatch(List.of(user1, user2));

        assertThat(result).hasSize(2);
        assertThat(result.get(user1).status()).isEqualTo(PresenceStatus.ONLINE);
        assertThat(result.get(user2).status()).isEqualTo(PresenceStatus.OFFLINE);
    }
}
