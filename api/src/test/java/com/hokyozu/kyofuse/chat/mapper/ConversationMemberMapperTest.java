package com.hokyozu.kyofuse.chat.mapper;

import com.hokyozu.kyofuse.chat.dto.response.ConversationMemberResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.ConversationMember;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationMemberMapperTest {

    @Test
    void toGroupAdminBuildsActiveAdminMembership() {
        Conversation conversation = conversation();
        User user = user("owner");

        ConversationMember member = ConversationMemberMapper.toGroupAdmin(conversation, user);

        assertThat(member.getConversation()).isEqualTo(conversation);
        assertThat(member.getUser()).isEqualTo(user);
        assertThat(member.getRole()).isEqualTo(ConversationMemberRole.ADMIN);
        assertThat(member.getStatus()).isEqualTo(ConversationMemberStatus.ACTIVE);
        assertThat(member.getJoinedAt()).isNotNull();
        assertThat(member.getLeftAt()).isNull();
        assertThat(member.getLastReadAt()).isNull();
        assertThat(member.getCreatedAt()).isNotNull();
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    void toGroupMemberBuildsActiveMemberMembership() {
        Conversation conversation = conversation();
        User user = user("participant");

        ConversationMember member = ConversationMemberMapper.toGroupMember(conversation, user);

        assertThat(member.getConversation()).isEqualTo(conversation);
        assertThat(member.getUser()).isEqualTo(user);
        assertThat(member.getRole()).isEqualTo(ConversationMemberRole.MEMBER);
        assertThat(member.getStatus()).isEqualTo(ConversationMemberStatus.ACTIVE);
        assertThat(member.getLeftAt()).isNull();
    }

    @Test
    void toResponseMapsAllFields() {
        Conversation conversation = conversation();
        User user = user("participant");
        Instant now = Instant.now();
        ConversationMember member = ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .user(user)
                .role(ConversationMemberRole.MEMBER)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(now)
                .leftAt(null)
                .lastReadAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        GamerProfile profile = GamerProfile.builder()
                .nickname("Participant")
                .avatarUrl("https://example.com/avatar.png")
                .build();

        ConversationMemberResponse response = ConversationMemberMapper.toResponse(member, profile);

        assertThat(response.id()).isEqualTo(member.getId());
        assertThat(response.conversationId()).isEqualTo(conversation.getId());
        assertThat(response.conversationName()).isEqualTo(conversation.getName());
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo("participant");
        assertThat(response.nickname()).isEqualTo("Participant");
        assertThat(response.avatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(response.role()).isEqualTo(ConversationMemberRole.MEMBER);
        assertThat(response.status()).isEqualTo(ConversationMemberStatus.ACTIVE);
        assertThat(response.joinedAt()).isEqualTo(now);
        assertThat(response.leftAt()).isNull();
        assertThat(response.lastReadAt()).isEqualTo(now);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    @Test
    void toResponseLeavesNicknameAndAvatarNullWhenProfileIsAbsent() {
        Instant now = Instant.now();
        ConversationMember member = ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(conversation())
                .user(user("participant"))
                .role(ConversationMemberRole.MEMBER)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ConversationMemberResponse response = ConversationMemberMapper.toResponse(member, null);

        assertThat(response.username()).isEqualTo("participant");
        assertThat(response.nickname()).isNull();
        assertThat(response.avatarUrl()).isNull();
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
