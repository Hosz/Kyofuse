package com.hokyozu.kyofuse.chat.controller;

import com.hokyozu.kyofuse.chat.dto.request.ConversationRequest;
import com.hokyozu.kyofuse.chat.dto.request.UpdateConversationRequest;
import com.hokyozu.kyofuse.chat.dto.response.ConversationResponse;
import com.hokyozu.kyofuse.chat.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/create")
    public ConversationResponse createConversation(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestBody @Valid ConversationRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationService.createConversation(request, userId);
    }

    @PatchMapping("/edit/{conversationId}")
    public ConversationResponse editGroupConversation(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable UUID conversationId,
                                                      @RequestBody @Valid UpdateConversationRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationService.editGroupConversation(conversationId, request, userId);
    }

    @PatchMapping("/{conversationId}/accept")
    public ConversationResponse acceptDirectConversation(@AuthenticationPrincipal Jwt jwt,
                                                          @PathVariable UUID conversationId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationService.acceptDirectConversation(conversationId, userId);
    }

    @PatchMapping("/{conversationId}/decline")
    public ConversationResponse declineDirectConversation(@AuthenticationPrincipal Jwt jwt,
                                                           @PathVariable UUID conversationId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationService.declineDirectConversation(conversationId, userId);
    }

    @PatchMapping("/{conversationId}/revoke")
    public ConversationResponse revokeDirectConversationPermission(@AuthenticationPrincipal Jwt jwt,
                                                                    @PathVariable UUID conversationId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationService.revokeDirectConversationPermission(conversationId, userId);
    }

    @GetMapping("/list-direct-conversations")
    public Page<ConversationResponse> listDirectConversations(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationService.listDirectConversations(userId, pageable);
    }

    @GetMapping("/list-community-conversation")
    public Page<ConversationResponse> listCommunityConversations(@AuthenticationPrincipal Jwt jwt,
                                                                 Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationService.listCommunityConversations(userId, pageable);
    }

    @GetMapping("/list-group-conversations")
    public Page<ConversationResponse> listGroupConversations(@AuthenticationPrincipal Jwt jwt,
                                                             Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationService.listGroupConversations(userId, pageable);
    }

    @GetMapping("/{conversationId}/details")
    public ConversationResponse getConversationDetails(@AuthenticationPrincipal Jwt jwt,
                                                       @PathVariable UUID conversationId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationService.getConversationDetails(conversationId, userId);
    }
}
