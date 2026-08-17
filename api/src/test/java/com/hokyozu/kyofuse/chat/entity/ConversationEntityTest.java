package com.hokyozu.kyofuse.chat.entity;

import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationEntityTest {

    @Test
    void shouldCreateDirectConversationWithAllFields() {
        UUID conversationId = UUID.randomUUID();
        User creator = createUser("creator");
        User userOne = createUser("userOne");
        User userTwo = createUser("userTwo");
        Instant now = Instant.now();

        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .type(ConversationType.DIRECT)
                .name(null)
                .createdBy(creator)
                .community(null)
                .directUserOne(userOne)
                .directUserTwo(userTwo)
                .directMessageStatus(DirectConversationStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(conversation.getId()).isEqualTo(conversationId);
        assertThat(conversation.getType()).isEqualTo(ConversationType.DIRECT);
        assertThat(conversation.getName()).isNull();
        assertThat(conversation.getCreatedBy()).isEqualTo(creator);
        assertThat(conversation.getCommunity()).isNull();
        assertThat(conversation.getDirectUserOne()).isEqualTo(userOne);
        assertThat(conversation.getDirectUserTwo()).isEqualTo(userTwo);
        assertThat(conversation.getDirectMessageStatus()).isEqualTo(DirectConversationStatus.PENDING);
        assertThat(conversation.getCreatedAt()).isEqualTo(now);
        assertThat(conversation.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateGroupConversationWithoutDirectOrCommunityFields() {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(createUser("creator"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        assertThat(conversation.getType()).isEqualTo(ConversationType.GROUP);
        assertThat(conversation.getName()).isEqualTo("Squad");
        assertThat(conversation.getCommunity()).isNull();
        assertThat(conversation.getDirectUserOne()).isNull();
        assertThat(conversation.getDirectUserTwo()).isNull();
        assertThat(conversation.getDirectMessageStatus()).isNull();
    }

    @Test
    void shouldCreateCommunityConversationWithCommunityLink() {
        Community community = Community.builder().id(UUID.randomUUID()).name("Kyofuse CS2").build();

        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.COMMUNITY)
                .name(null)
                .createdBy(createUser("creator"))
                .community(community)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        assertThat(conversation.getType()).isEqualTo(ConversationType.COMMUNITY);
        assertThat(conversation.getCommunity()).isEqualTo(community);
        assertThat(conversation.getDirectUserOne()).isNull();
        assertThat(conversation.getDirectUserTwo()).isNull();
    }

    @Test
    void shouldUpdateMutableFields() {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.DIRECT)
                .createdBy(createUser("creator"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Instant updated = Instant.now().plusSeconds(60);
        conversation.setDirectMessageStatus(DirectConversationStatus.ACCEPTED);
        conversation.setUpdatedAt(updated);

        assertThat(conversation.getDirectMessageStatus()).isEqualTo(DirectConversationStatus.ACCEPTED);
        assertThat(conversation.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void shouldHandleAllConversationTypesAndDirectStatuses() {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.DIRECT)
                .createdBy(createUser("creator"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        for (ConversationType type : ConversationType.values()) {
            conversation.setType(type);
            assertThat(conversation.getType()).isEqualTo(type);
        }

        for (DirectConversationStatus status : DirectConversationStatus.values()) {
            conversation.setDirectMessageStatus(status);
            assertThat(conversation.getDirectMessageStatus()).isEqualTo(status);
        }
    }

    private User createUser(String username) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(username + "@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
