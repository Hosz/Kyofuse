package com.hokyozu.kyofuse.chat.controller;

import com.hokyozu.kyofuse.chat.dto.request.MessageRequest;
import com.hokyozu.kyofuse.chat.dto.response.MessageResponse;
import com.hokyozu.kyofuse.chat.service.MessageService;
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
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController controller;

    @Test
    void sendMessageUsesAuthenticatedUserIdAndPathConversationId() {
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MessageRequest request = new MessageRequest("hello there");
        MessageResponse expected = response();
        when(messageService.sendMessage(conversationId, request, userId)).thenReturn(expected);

        MessageResponse result = controller.sendMessage(conversationId, request, jwt(userId));

        assertThat(result).isSameAs(expected);
        verify(messageService).sendMessage(conversationId, request, userId);
    }

    @Test
    void getMessagesUsesAuthenticatedUserIdAndPathConversationId() {
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<MessageResponse> expected = new PageImpl<>(List.of(response()));
        when(messageService.getMessages(conversationId, userId, pageable)).thenReturn(expected);

        Page<MessageResponse> result = controller.getMessages(conversationId, jwt(userId), pageable);

        assertThat(result).isSameAs(expected);
        verify(messageService).getMessages(conversationId, userId, pageable);
    }

    @Test
    void deleteMessageUsesAuthenticatedUserIdAndPathIds() {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        controller.deleteMessage(conversationId, messageId, jwt(userId));

        verify(messageService).deleteMessage(userId, conversationId, messageId);
    }

    private MessageResponse response() {
        return new MessageResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "player",
                "Player",
                "https://example.com/avatar.png",
                "hello there",
                List.of(),
                Instant.now(),
                com.hokyozu.kyofuse.chat.enums.MessageStatus.SENT
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
