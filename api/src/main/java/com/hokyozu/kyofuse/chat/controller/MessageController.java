package com.hokyozu.kyofuse.chat.controller;

import com.hokyozu.kyofuse.chat.dto.request.MessageRequest;
import com.hokyozu.kyofuse.chat.dto.response.MessageResponse;
import com.hokyozu.kyofuse.chat.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

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
}
