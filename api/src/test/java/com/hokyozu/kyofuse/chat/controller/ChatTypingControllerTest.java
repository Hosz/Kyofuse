package com.hokyozu.kyofuse.chat.controller;

import com.hokyozu.kyofuse.chat.dto.event.TypingEvent;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.Jwt;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatTypingControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserFinder userFinder;

    @Mock
    private GamerProfileFinder gamerProfileFinder;

    @InjectMocks
    private ChatTypingController controller;

    @Test
    void handleTypingBroadcastsTypingEvent() {
        UUID convId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Principal principal = () -> userId.toString();

        User user = User.builder().id(userId).username("player1").build();
        GamerProfile profile = GamerProfile.builder().nickname("Nick1").build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);

        controller.handleTyping(convId, Map.of("typing", true), principal);

        TypingEvent expected = new TypingEvent(convId, userId, "player1", "Nick1", true);
        verify(messagingTemplate).convertAndSend(eq("/topic/conversations/" + convId + "/typing"), eq(expected));
    }

    @Test
    void handleTypingRestBroadcastsTypingEvent() {
        UUID convId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        User user = User.builder().id(userId).username("player1").build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        controller.handleTypingRest(convId, Map.of("isTyping", true), jwt);

        TypingEvent expected = new TypingEvent(convId, userId, "player1", "player1", true);
        verify(messagingTemplate).convertAndSend(eq("/topic/conversations/" + convId + "/typing"), eq(expected));
    }
}
