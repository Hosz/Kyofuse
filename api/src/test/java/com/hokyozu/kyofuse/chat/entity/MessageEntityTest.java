package com.hokyozu.kyofuse.chat.entity;

import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageEntityTest {

    @Test
    void shouldCreateMessageWithAllFields() {
        UUID messageId = UUID.randomUUID();
        Conversation conversation = createConversation();
        User sender = createUser();
        Instant now = Instant.now();

        Message message = Message.builder()
                .id(messageId)
                .conversation(conversation)
                .sender(sender)
                .content("hello there")
                .createdAt(now)
                .build();

        assertThat(message.getId()).isEqualTo(messageId);
        assertThat(message.getConversation()).isEqualTo(conversation);
        assertThat(message.getSender()).isEqualTo(sender);
        assertThat(message.getContent()).isEqualTo("hello there");
        assertThat(message.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateContentViaSetter() {
        Message message = Message.builder()
                .id(UUID.randomUUID())
                .conversation(createConversation())
                .sender(createUser())
                .content("original")
                .createdAt(Instant.now())
                .build();

        message.setContent("edited");

        assertThat(message.getContent()).isEqualTo("edited");
    }

    private Conversation createConversation() {
        return Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(createUser())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("player")
                .email("player@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
