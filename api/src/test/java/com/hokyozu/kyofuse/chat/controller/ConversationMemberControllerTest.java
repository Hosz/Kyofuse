package com.hokyozu.kyofuse.chat.controller;

import com.hokyozu.kyofuse.chat.dto.response.ConversationMemberResponse;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.service.ConversationMemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationMemberControllerTest {

    @Mock
    private ConversationMemberService conversationMemberService;

    @InjectMocks
    private ConversationMemberController controller;

    @Test
    void addMemberToConversationUsesAuthenticatedUserId() {
        UUID conversationId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        controller.addMemberToConversation(conversationId, memberId, jwt(userId));

        verify(conversationMemberService).addMemberToConversation(conversationId, memberId, userId);
    }

    @Test
    void listConversationMembersUsesAuthenticatedUserId() {
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversationMemberResponse> expected = new PageImpl<>(List.of(response()));
        when(conversationMemberService.listConversationMembers(conversationId, userId, pageable)).thenReturn(expected);

        Page<ConversationMemberResponse> result = controller.listConversationMembers(conversationId, jwt(userId), pageable);

        assertThat(result).isSameAs(expected);
        verify(conversationMemberService).listConversationMembers(conversationId, userId, pageable);
    }

    @Test
    void promoteMemberToAdminUsesAuthenticatedUserId() {
        UUID conversationId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        controller.promoteMemberToAdmin(conversationId, memberId, jwt(userId));

        verify(conversationMemberService).promoteMemberToAdmin(conversationId, memberId, userId);
    }

    @Test
    void demoteAdminToMemberUsesAuthenticatedUserId() {
        UUID conversationId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        controller.demoteAdminToMember(conversationId, memberId, jwt(userId));

        verify(conversationMemberService).demoteAdminToMember(conversationId, memberId, userId);
    }

    @Test
    void removeMemberFromConversationUsesAuthenticatedUserId() {
        UUID conversationId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        controller.removeMemberFromConversation(conversationId, memberId, jwt(userId));

        verify(conversationMemberService).removeMemberFromConversation(conversationId, memberId, userId);
    }

    @Test
    void leaveConversationUsesAuthenticatedUserId() {
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        controller.leaveConversation(conversationId, jwt(userId));

        verify(conversationMemberService).leaveConversation(conversationId, userId);
    }

    private ConversationMemberResponse response() {
        Instant now = Instant.now();
        return new ConversationMemberResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Squad",
                UUID.randomUUID(),
                "player",
                ConversationMemberRole.MEMBER,
                ConversationMemberStatus.ACTIVE,
                now,
                null,
                null,
                now,
                now
        );
    }

    private Jwt jwt(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
