package com.hokyozu.kyofuse.presence.controller;

import com.hokyozu.kyofuse.presence.dto.request.BatchPresenceRequest;
import com.hokyozu.kyofuse.presence.dto.response.PresenceResponse;
import com.hokyozu.kyofuse.presence.enums.PresenceStatus;
import com.hokyozu.kyofuse.presence.service.UserPresenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceControllerTest {

    @Mock
    private UserPresenceService userPresenceService;

    @InjectMocks
    private PresenceController presenceController;

    @Test
    void getPresenceReturnsPresenceResponse() {
        UUID userId = UUID.randomUUID();
        PresenceResponse expected = new PresenceResponse(userId, PresenceStatus.ONLINE, Instant.now());
        when(userPresenceService.getPresence(userId)).thenReturn(expected);

        ResponseEntity<PresenceResponse> response = presenceController.getPresence(userId);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void getPresenceBatchReturnsMap() {
        UUID userId = UUID.randomUUID();
        BatchPresenceRequest request = new BatchPresenceRequest(List.of(userId));
        Map<UUID, PresenceResponse> expected = Map.of(userId, new PresenceResponse(userId, PresenceStatus.ONLINE, Instant.now()));
        when(userPresenceService.getPresenceBatch(request.userIds())).thenReturn(expected);

        ResponseEntity<Map<UUID, PresenceResponse>> response = presenceController.getPresenceBatch(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void handleHeartbeatInvokesService() {
        UUID userId = UUID.randomUUID();
        Principal principal = () -> userId.toString();

        presenceController.handleHeartbeat(principal);

        verify(userPresenceService).heartbeat(userId);
    }

    @Test
    void heartbeatHttpInvokesService() {
        UUID userId = UUID.randomUUID();
        Principal principal = () -> userId.toString();

        ResponseEntity<Void> response = presenceController.heartbeat(principal);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(userPresenceService).heartbeat(userId);
    }
}
