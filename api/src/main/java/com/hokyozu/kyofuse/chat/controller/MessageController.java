package com.hokyozu.kyofuse.chat.controller;

import com.hokyozu.kyofuse.chat.dto.request.MessageRequest;
import com.hokyozu.kyofuse.chat.dto.response.MessageInfoResponse;
import com.hokyozu.kyofuse.chat.dto.response.MessageResponse;
import com.hokyozu.kyofuse.chat.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @com.hokyozu.kyofuse.infrastructure.ratelimit.RateLimit(key = "send_message", limit = 30, period = 60, type = com.hokyozu.kyofuse.infrastructure.ratelimit.RateLimitType.USER_ID)
    @PostMapping("/{conversationId}/send-message")
    public MessageResponse sendMessage(@PathVariable UUID conversationId,
                                       @RequestBody @Valid MessageRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return messageService.sendMessage(conversationId, request, userId);
    }

    @GetMapping("/{conversationId}/messages")
    public Page<MessageResponse> getMessages(@PathVariable UUID conversationId,
                                             @AuthenticationPrincipal Jwt jwt,
                                             Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return messageService.getMessages(conversationId, userId, pageable);
    }

    @DeleteMapping("/{conversationId}/delete/{messageId}")
    public void deleteMessage(@PathVariable UUID conversationId,
                              @PathVariable UUID messageId,
                              @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        messageService.deleteMessage(userId, conversationId, messageId);
    }

    @PostMapping("/{conversationId}/read")
    public void markConversationAsRead(@PathVariable UUID conversationId,
                                                       @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        messageService.markConversationAsRead(conversationId, userId);
    }

    @PostMapping("/delivered")
    public void markDelivered(@RequestBody List<UUID> messageIds,
                              @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        messageService.markMessagesAsDelivered(messageIds, userId);
    }

    @GetMapping("/{conversationId}/info/{messageId}")
    public MessageInfoResponse getMessageInfo(@PathVariable UUID conversationId,
                                              @PathVariable UUID messageId,
                                              @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return messageService.getMessageInfo(conversationId, messageId, userId);
    }
}
