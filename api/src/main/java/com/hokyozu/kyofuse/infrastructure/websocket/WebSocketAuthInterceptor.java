package com.hokyozu.kyofuse.infrastructure.websocket;

import com.hokyozu.kyofuse.chat.service.ConversationPermissionService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AuthCookieService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtAuthConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Pattern CONVERSATION_TOPIC_PATTERN =
            Pattern.compile("^/topic/conversations/([a-fA-F0-9\\-]+)(?:/.*)?$");

    private final JwtDecoder jwtDecoder;
    private final JwtAuthConverter jwtAuthConverter;
    private final ConversationPermissionService conversationPermissionService;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                authenticateConnect(accessor);
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                authorizeSubscription(accessor);
            }
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);

        if (token != null) {
            try {
                Jwt jwt = jwtDecoder.decode(token);
                AbstractAuthenticationToken authentication = jwtAuthConverter.convert(jwt);
                accessor.setUser(authentication);
                log.info("WebSocket connection authenticated for user: {}", authentication.getName());
            } catch (Exception e) {
                log.warn("WebSocket connection authentication failed: {}", e.getMessage());
                throw new AuthenticationServiceException("WebSocket connection authentication failed");
            }
        } else {
            log.warn("WebSocket connection authentication failed: missing token");
            throw new AuthenticationServiceException("WebSocket connection authentication failed");
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/conversations")) {
            return;
        }

        if ("/topic/conversations".equals(destination) || "/topic/conversations/".equals(destination)) {
            log.warn("Subscription denied: generic destination '{}'", destination);
            throw new AccessDeniedException("Invalid conversation destination");
        }

        Matcher matcher = CONVERSATION_TOPIC_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            log.warn("Subscription denied: malformed conversation destination '{}'", destination);
            throw new AccessDeniedException("Invalid conversation destination format");
        }

        UUID conversationId;
        try {
            conversationId = UUID.fromString(matcher.group(1));
        } catch (IllegalArgumentException e) {
            log.warn("Subscription denied: invalid UUID in destination '{}'", destination);
            throw new AccessDeniedException("Invalid conversation ID format");
        }

        Principal principal = accessor.getUser();
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            log.warn("Subscription denied: unauthenticated attempt to subscribe to '{}'", destination);
            throw new AccessDeniedException("User is not authenticated");
        }

        UUID userId;
        try {
            userId = UUID.fromString(principal.getName());
        } catch (IllegalArgumentException e) {
            log.warn("Subscription denied: invalid principal user ID '{}'", principal.getName());
            throw new AccessDeniedException("Invalid principal user ID");
        }

        if (!conversationPermissionService.isParticipant(conversationId, userId)) {
            log.warn("Subscription denied: user {} is not an active participant in conversation {}", userId, conversationId);
            throw new AccessDeniedException("User is not authorized to subscribe to this conversation");
        }

        log.debug("Subscription authorized: user {} for conversation {}", userId, conversationId);
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey(AuthCookieService.ACCESS_TOKEN_COOKIE)) {
            return (String) sessionAttributes.get(AuthCookieService.ACCESS_TOKEN_COOKIE);
        }

        return null;
    }
}
