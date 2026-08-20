package com.hokyozu.kyofuse.chat.controller;

import com.hokyozu.kyofuse.chat.dto.response.ConversationMemberResponse;
import com.hokyozu.kyofuse.chat.service.ConversationMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/members")
@RequiredArgsConstructor
public class ConversationMemberController {

    private final ConversationMemberService conversationMemberService;

    @PostMapping("/{conversationId}/add/{memberId}")
    public void addMemberToConversation(@PathVariable UUID conversationId,
                                        @PathVariable UUID memberId,
                                        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        conversationMemberService.addMemberToConversation(conversationId, memberId, userId);
    }

    @GetMapping("/{conversationId}/list-members")
    public Page<ConversationMemberResponse> listConversationMembers(@PathVariable UUID conversationId,
                                                                    @AuthenticationPrincipal Jwt jwt,
                                                                    Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return conversationMemberService.listConversationMembers(conversationId, userId, pageable);
    }

    @PatchMapping("/{conversationId}/promote/{memberId}")
    public void promoteMemberToAdmin(@PathVariable UUID conversationId,
                                     @PathVariable UUID memberId,
                                     @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        conversationMemberService.promoteMemberToAdmin(conversationId, memberId, userId);
    }

    @PatchMapping("/{conversationId}/demote/{memberId}")
    public void demoteAdminToMember(@PathVariable UUID conversationId,
                                    @PathVariable UUID memberId,
                                    @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        conversationMemberService.demoteAdminToMember(conversationId, memberId, userId);
    }

    @DeleteMapping("/{conversationId}/remove/{memberId}")
    public void removeMemberFromConversation(@PathVariable UUID conversationId,
                                             @PathVariable UUID memberId,
                                             @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        conversationMemberService.removeMemberFromConversation(conversationId, memberId, userId);
    }

    @DeleteMapping("/{conversationId}/leave")
    public void leaveConversation(@PathVariable UUID conversationId,
                                  @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        conversationMemberService.leaveConversation(conversationId, userId);
    }
}
