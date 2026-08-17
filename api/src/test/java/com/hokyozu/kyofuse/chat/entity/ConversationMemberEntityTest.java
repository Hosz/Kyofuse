package com.hokyozu.kyofuse.chat.entity;

import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationMemberEntityTest {

    @Test
    void shouldCreateConversationMemberWithAllFields() {
        UUID memberId = UUID.randomUUID();
        Conversation conversation = createConversation();
        User user = createUser();
        Instant now = Instant.now();

        ConversationMember member = ConversationMember.builder()
                .id(memberId)
                .conversation(conversation)
                .user(user)
                .role(ConversationMemberRole.ADMIN)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(now)
                .leftAt(null)
                .lastReadAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(member.getId()).isEqualTo(memberId);
        assertThat(member.getConversation()).isEqualTo(conversation);
        assertThat(member.getUser()).isEqualTo(user);
        assertThat(member.getRole()).isEqualTo(ConversationMemberRole.ADMIN);
        assertThat(member.getStatus()).isEqualTo(ConversationMemberStatus.ACTIVE);
        assertThat(member.getJoinedAt()).isEqualTo(now);
        assertThat(member.getLeftAt()).isNull();
        assertThat(member.getLastReadAt()).isEqualTo(now);
        assertThat(member.getCreatedAt()).isEqualTo(now);
        assertThat(member.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldAllowNullOptionalFields() {
        ConversationMember member = ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(createConversation())
                .user(createUser())
                .role(ConversationMemberRole.MEMBER)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        assertThat(member.getLeftAt()).isNull();
        assertThat(member.getLastReadAt()).isNull();
    }

    @Test
    void shouldUpdateStatusRoleAndLeftAtWhenMemberLeaves() {
        ConversationMember member = ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(createConversation())
                .user(createUser())
                .role(ConversationMemberRole.ADMIN)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Instant leftAt = Instant.now().plusSeconds(120);
        member.setStatus(ConversationMemberStatus.LEFT);
        member.setRole(ConversationMemberRole.MEMBER);
        member.setLeftAt(leftAt);

        assertThat(member.getStatus()).isEqualTo(ConversationMemberStatus.LEFT);
        assertThat(member.getRole()).isEqualTo(ConversationMemberRole.MEMBER);
        assertThat(member.getLeftAt()).isEqualTo(leftAt);
    }

    @Test
    void shouldHandleAllRoleAndStatusValues() {
        ConversationMember member = ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(createConversation())
                .user(createUser())
                .role(ConversationMemberRole.MEMBER)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        for (ConversationMemberRole role : ConversationMemberRole.values()) {
            member.setRole(role);
            assertThat(member.getRole()).isEqualTo(role);
        }

        for (ConversationMemberStatus status : ConversationMemberStatus.values()) {
            member.setStatus(status);
            assertThat(member.getStatus()).isEqualTo(status);
        }
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
