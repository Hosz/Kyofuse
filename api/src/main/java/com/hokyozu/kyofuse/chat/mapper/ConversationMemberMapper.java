package com.hokyozu.kyofuse.chat.mapper;

import com.hokyozu.kyofuse.chat.dto.response.ConversationMemberResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.ConversationMember;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class ConversationMemberMapper {
    public static ConversationMember toGroupAdmin(Conversation conversation, User user) {
        return ConversationMember.builder()
                .conversation(conversation)
                .user(user)
                .role(ConversationMemberRole.ADMIN)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .leftAt(null)
                .lastReadAt(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static ConversationMember toGroupMember(Conversation conversation, User participant) {
        return ConversationMember.builder()
                .conversation(conversation)
                .user(participant)
                .role(ConversationMemberRole.MEMBER)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .leftAt(null)
                .lastReadAt(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * gamerProfile pode vir null quando o chamador não precisa da identidade visual
     * do membro — nesse caso nickname e avatar saem nulos.
     */
    public static ConversationMemberResponse toResponse(ConversationMember conversationMember, GamerProfile gamerProfile) {
        return new ConversationMemberResponse(
                conversationMember.getId(),
                conversationMember.getConversation().getId(),
                conversationMember.getConversation().getName(),
                conversationMember.getUser().getId(),
                conversationMember.getUser().getUsername(),
                gamerProfile != null ? gamerProfile.getNickname() : null,
                gamerProfile != null ? gamerProfile.getAvatarUrl() : null,
                conversationMember.getRole(),
                conversationMember.getStatus(),
                conversationMember.getJoinedAt(),
                conversationMember.getLeftAt(),
                conversationMember.getLastReadAt(),
                conversationMember.getCreatedAt(),
                conversationMember.getUpdatedAt()
        );
    }
}
