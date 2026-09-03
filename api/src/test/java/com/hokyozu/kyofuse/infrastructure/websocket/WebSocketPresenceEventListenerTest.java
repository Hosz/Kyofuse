package com.hokyozu.kyofuse.infrastructure.websocket;

import com.hokyozu.kyofuse.presence.service.UserPresenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.CloseStatus;

import java.security.Principal;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketPresenceEventListenerTest {

    @Mock
    private UserPresenceService userPresenceService;

    @InjectMocks
    private WebSocketPresenceEventListener listener;

    @Test
    void handleWebSocketConnectListenerRegistersConnect() {
        UUID userId = UUID.randomUUID();
        Principal principal = () -> userId.toString();
        String sessionId = "sess-123";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
        accessor.setUser(principal);
        accessor.setSessionId(sessionId);
        Message<byte[]> message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionConnectedEvent event = new SessionConnectedEvent(this, message, principal);

        listener.handleWebSocketConnectListener(event);

        verify(userPresenceService).registerConnect(userId, sessionId);
    }

    @Test
    void handleWebSocketDisconnectListenerRegistersDisconnect() {
        UUID userId = UUID.randomUUID();
        Principal principal = () -> userId.toString();
        String sessionId = "sess-123";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setUser(principal);
        accessor.setSessionId(sessionId);
        Message<byte[]> message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, sessionId, CloseStatus.NORMAL, principal);

        listener.handleWebSocketDisconnectListener(event);

        verify(userPresenceService).registerDisconnect(userId, sessionId);
    }
}
