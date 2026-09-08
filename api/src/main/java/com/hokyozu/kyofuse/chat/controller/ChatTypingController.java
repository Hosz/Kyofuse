package com.hokyozu.kyofuse.chat.controller;

import com.hokyozu.kyofuse.chat.dto.event.TypingEvent;
import com.hokyozu.kyofuse.chat.service.ConversationPermissionService;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatTypingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserFinder userFinder;
    private final GamerProfileFinder gamerProfileFinder;
    private final ConversationPermissionService conversationPermissionService;

    @MessageMapping("/chat/{conversationId}/typing")
    public void handleTyping(
            @DestinationVariable UUID conversationId,
            @Payload Map<String, Boolean> payload,
            Principal principal
    ) {
        if (principal == null) return;
        try {
            UUID userId = UUID.fromString(principal.getName());
            if (!conversationPermissionService.isParticipant(conversationId, userId)) {
                log.warn("Unauthorized typing event: user {} is not an active participant in conversation {}", userId, conversationId);
                return;
            }
            boolean isTyping = Boolean.TRUE.equals(payload.get("isTyping")) || Boolean.TRUE.equals(payload.get("typing"));
            broadcastTyping(conversationId, userId, isTyping);
        } catch (Exception e) {
            log.warn("Erro ao processar typing websocket: {}", e.getMessage());
        }
    }

    @PostMapping("/api/messages/{conversationId}/typing")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleTypingRest(
            @PathVariable UUID conversationId,
            @RequestBody Map<String, Boolean> payload,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        if (!conversationPermissionService.isParticipant(conversationId, userId)) {
            throw new ForbiddenException("User is not an active participant in this conversation.");
        }
        boolean isTyping = Boolean.TRUE.equals(payload.get("isTyping")) || Boolean.TRUE.equals(payload.get("typing"));
        broadcastTyping(conversationId, userId, isTyping);
    }

    private void broadcastTyping(UUID conversationId, UUID userId, boolean isTyping) {
        try {
            User user = userFinder.findProfileByUserId(userId);
            GamerProfile profile = gamerProfileFinder.findProfileByUserId(userId);
            String nickname = profile != null ? profile.getNickname() : user.getUsername();

            TypingEvent event = new TypingEvent(conversationId, userId, user.getUsername(), nickname, isTyping);
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/typing", event);
        } catch (Exception e) {
            log.warn("Erro ao transmitir typing event: {}", e.getMessage());
        }
    }
}
