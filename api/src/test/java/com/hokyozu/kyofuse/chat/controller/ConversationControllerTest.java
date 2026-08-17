package com.hokyozu.kyofuse.chat.controller;

import com.hokyozu.kyofuse.chat.dto.request.ConversationRequest;
import com.hokyozu.kyofuse.chat.dto.response.ConversationResponse;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.chat.service.ConversationService;
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
class ConversationControllerTest {

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private ConversationController controller;

    @Test
    void createConversationUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        ConversationRequest request = new ConversationRequest("Squad", List.of(UUID.randomUUID(), UUID.randomUUID()));
        ConversationResponse expected = response();
        when(conversationService.createConversation(request, userId)).thenReturn(expected);

        ConversationResponse result = controller.createConversation(jwt(userId), request);

        assertThat(result).isSameAs(expected);
        verify(conversationService).createConversation(request, userId);
    }

    @Test
    void acceptDirectConversationUsesAuthenticatedUserIdAndPathConversationId() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        ConversationResponse expected = response();
        when(conversationService.acceptDirectConversation(conversationId, userId)).thenReturn(expected);

        ConversationResponse result = controller.acceptDirectConversation(jwt(userId), conversationId);

        assertThat(result).isSameAs(expected);
        verify(conversationService).acceptDirectConversation(conversationId, userId);
    }

    @Test
    void declineDirectConversationUsesAuthenticatedUserIdAndPathConversationId() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        ConversationResponse expected = response();
        when(conversationService.declineDirectConversation(conversationId, userId)).thenReturn(expected);

        ConversationResponse result = controller.declineDirectConversation(jwt(userId), conversationId);

        assertThat(result).isSameAs(expected);
        verify(conversationService).declineDirectConversation(conversationId, userId);
    }

    @Test
    void revokeDirectConversationPermissionUsesAuthenticatedUserIdAndPathConversationId() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        ConversationResponse expected = response();
        when(conversationService.revokeDirectConversationPermission(conversationId, userId)).thenReturn(expected);

        ConversationResponse result = controller.revokeDirectConversationPermission(jwt(userId), conversationId);

        assertThat(result).isSameAs(expected);
        verify(conversationService).revokeDirectConversationPermission(conversationId, userId);
    }

    @Test
    void listDirectConversationsUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversationResponse> expected = new PageImpl<>(List.of(response()));
        when(conversationService.listDirectConversations(userId, pageable)).thenReturn(expected);

        Page<ConversationResponse> result = controller.listDirectConversations(jwt(userId), pageable);

        assertThat(result).isSameAs(expected);
        verify(conversationService).listDirectConversations(userId, pageable);
    }

    @Test
    void listCommunityConversationsUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversationResponse> expected = new PageImpl<>(List.of(response()));
        when(conversationService.listCommunityConversations(userId, pageable)).thenReturn(expected);

        Page<ConversationResponse> result = controller.listCommunityConversations(jwt(userId), pageable);

        assertThat(result).isSameAs(expected);
        verify(conversationService).listCommunityConversations(userId, pageable);
    }

    @Test
    void listGroupConversationsUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversationResponse> expected = new PageImpl<>(List.of(response()));
        when(conversationService.listGroupConversations(userId, pageable)).thenReturn(expected);

        Page<ConversationResponse> result = controller.listGroupConversations(jwt(userId), pageable);

        assertThat(result).isSameAs(expected);
        verify(conversationService).listGroupConversations(userId, pageable);
    }

    @Test
    void getConversationDetailsUsesAuthenticatedUserIdAndPathConversationId() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        ConversationResponse expected = response();
        when(conversationService.getConversationDetails(conversationId, userId)).thenReturn(expected);

        ConversationResponse result = controller.getConversationDetails(jwt(userId), conversationId);

        assertThat(result).isSameAs(expected);
        verify(conversationService).getConversationDetails(conversationId, userId);
    }

    private ConversationResponse response() {
        Instant now = Instant.now();
        return new ConversationResponse(
                UUID.randomUUID(),
                ConversationType.GROUP,
                "Squad",
                UUID.randomUUID(),
                "creator",
                null,
                null,
                null,
                null,
                null,
                null,
                DirectConversationStatus.ACCEPTED,
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
