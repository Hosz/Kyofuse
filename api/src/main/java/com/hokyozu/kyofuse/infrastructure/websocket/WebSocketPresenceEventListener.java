package com.hokyozu.kyofuse.infrastructure.websocket;

import com.hokyozu.kyofuse.presence.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketPresenceEventListener {

    private final UserPresenceService userPresenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser() != null ? accessor.getUser() : event.getUser();
        String sessionId = accessor.getSessionId();

        if (user != null && sessionId != null) {
            try {
                UUID userId = extractUserId(user);
                userPresenceService.registerConnect(userId, sessionId);
                log.debug("User {} connected on WebSocket session {}", userId, sessionId);
            } catch (Exception e) {
                log.warn("Failed to register presence connect: {}", e.getMessage());
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser() != null ? accessor.getUser() : event.getUser();
        String sessionId = accessor.getSessionId() != null ? accessor.getSessionId() : event.getSessionId();

        if (user != null && sessionId != null) {
            try {
                UUID userId = extractUserId(user);
                userPresenceService.registerDisconnect(userId, sessionId);
                log.debug("User {} disconnected from WebSocket session {}", userId, sessionId);
            } catch (Exception e) {
                log.warn("Failed to register presence disconnect: {}", e.getMessage());
            }
        }
    }

    private UUID extractUserId(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}
