package com.hokyozu.kyofuse.infrastructure.websocket;

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
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthConverter jwtAuthConverter;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
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
                log.warn("WebSocket connection authentication failed");
                throw new AuthenticationServiceException("WebSocket connection authentication failed");
            }
        }

        return message;
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
