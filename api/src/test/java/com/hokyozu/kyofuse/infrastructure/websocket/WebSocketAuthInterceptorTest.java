package com.hokyozu.kyofuse.infrastructure.websocket;

import com.hokyozu.kyofuse.chat.service.ConversationPermissionService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AuthCookieService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtAuthConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private JwtAuthConverter jwtAuthConverter;

    @Mock
    private ConversationPermissionService conversationPermissionService;

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private WebSocketAuthInterceptor interceptor;

    private UUID userId;
    private UUID convId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        convId = UUID.randomUUID();
    }

    @Test
    void preSend_connect_withBearerToken_authenticatesSuccessfully() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("Authorization", "Bearer valid.token.here");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Jwt jwt = Jwt.withTokenValue("valid.token.here")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        AbstractAuthenticationToken auth = mock(AbstractAuthenticationToken.class);
        when(auth.getName()).thenReturn(userId.toString());
        when(jwtDecoder.decode("valid.token.here")).thenReturn(jwt);
        when(jwtAuthConverter.convert(jwt)).thenReturn(auth);

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertNotNull(result);
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertNotNull(resultAccessor.getUser());
        assertEquals(userId.toString(), resultAccessor.getUser().getName());
    }

    @Test
    void preSend_connect_withCookieToken_authenticatesSuccessfully() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AuthCookieService.ACCESS_TOKEN_COOKIE, "cookie.token.here");
        accessor.setSessionAttributes(sessionAttrs);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Jwt jwt = Jwt.withTokenValue("cookie.token.here")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        AbstractAuthenticationToken auth = mock(AbstractAuthenticationToken.class);
        when(auth.getName()).thenReturn(userId.toString());
        when(jwtDecoder.decode("cookie.token.here")).thenReturn(jwt);
        when(jwtAuthConverter.convert(jwt)).thenReturn(auth);

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertNotNull(result);
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertNotNull(resultAccessor.getUser());
        assertEquals(userId.toString(), resultAccessor.getUser().getName());
    }

    @Test
    void preSend_connect_missingToken_throwsException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AuthenticationServiceException.class, () ->
                interceptor.preSend(message, messageChannel)
        );
    }

    @Test
    void preSend_subscribe_publicTopic_allowedWithoutConversationCheck() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/presence");
        accessor.setUser(() -> userId.toString());
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertNotNull(result);
        verifyNoInteractions(conversationPermissionService);
    }

    @Test
    void preSend_subscribe_genericConversationDestination_denied() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations");
        accessor.setUser(() -> userId.toString());
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AccessDeniedException.class, () ->
                interceptor.preSend(message, messageChannel)
        );
    }

    @Test
    void preSend_subscribe_conversation_unauthenticatedUser_denied() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/" + convId);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AccessDeniedException.class, () ->
                interceptor.preSend(message, messageChannel)
        );
    }

    @Test
    void preSend_subscribe_conversation_authorizedParticipant_allowed() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/" + convId);
        accessor.setUser(() -> userId.toString());
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(conversationPermissionService.isParticipant(convId, userId)).thenReturn(true);

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertNotNull(result);
        verify(conversationPermissionService).isParticipant(convId, userId);
    }

    @Test
    void preSend_subscribe_conversationTyping_authorizedParticipant_allowed() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/" + convId + "/typing");
        accessor.setUser(() -> userId.toString());
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(conversationPermissionService.isParticipant(convId, userId)).thenReturn(true);

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertNotNull(result);
        verify(conversationPermissionService).isParticipant(convId, userId);
    }

    @Test
    void preSend_subscribe_conversationReactions_unauthorizedUser_denied() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/" + convId + "/reactions");
        accessor.setUser(() -> userId.toString());
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(conversationPermissionService.isParticipant(convId, userId)).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                interceptor.preSend(message, messageChannel)
        );

        assertTrue(ex.getMessage().contains("User is not authorized to subscribe"));
    }
}
