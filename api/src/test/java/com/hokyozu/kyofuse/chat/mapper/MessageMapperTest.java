package com.hokyozu.kyofuse.chat.mapper;

import com.hokyozu.kyofuse.chat.dto.request.MessageRequest;
import com.hokyozu.kyofuse.chat.dto.response.MessageResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.Message;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageMapperTest {

    @Test
    void toEntityBuildsMessageFromRequest() {
        Conversation conversation = conversation();
        User sender = user("sender");
        MessageRequest request = new MessageRequest("hello there");

        Message message = MessageMapper.toEntity(conversation, sender, request);

        assertThat(message.getConversation()).isEqualTo(conversation);
        assertThat(message.getSender()).isEqualTo(sender);
        assertThat(message.getContent()).isEqualTo("hello there");
        assertThat(message.getCreatedAt()).isNotNull();
    }

    @Test
    void toResponseMapsAllFields() {
        Conversation conversation = conversation();
        User sender = user("sender");
        Instant now = Instant.now();
        Message message = Message.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .sender(sender)
                .content("hello there")
                .createdAt(now)
                .build();

        MessageResponse response = MessageMapper.toResponse(message);

        assertThat(response.id()).isEqualTo(message.getId());
        assertThat(response.conversationId()).isEqualTo(conversation.getId());
        assertThat(response.senderId()).isEqualTo(sender.getId());
        assertThat(response.senderUsername()).isEqualTo("sender");
        assertThat(response.content()).isEqualTo("hello there");
        assertThat(response.createdAt()).isEqualTo(now);
    }

    private Conversation conversation() {
        return Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(user("creator"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private User user(String username) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(username + "@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
