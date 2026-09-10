package com.hokyozu.kyofuse.communities.mapper;

import com.hokyozu.kyofuse.communities.dto.request.CommunityInviteRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityInviteResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityInvite;
import com.hokyozu.kyofuse.communities.enums.CommunityInviteStatus;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class CommunityInviteMapper {

    public static CommunityInvite toEntity(Community community, User sender, User receiver, CommunityInviteRequest request) {
        return CommunityInvite.builder()
                .community(community)
                .sender(sender)
                .receiver(receiver)
                .status(CommunityInviteStatus.PENDING)
                .message(request != null ? request.message() : null)
                .createdAt(Instant.now())
                .build();
    }

    public static CommunityInviteResponse toResponse(CommunityInvite invite) {
        return new CommunityInviteResponse(
                invite.getId(),
                invite.getCommunity().getId(),
                invite.getCommunity().getName(),
                invite.getCommunity().getSlug(),
                invite.getCommunity().getAvatarUrl(),
                invite.getSender().getId(),
                invite.getSender().getUsername(),
                invite.getReceiver().getId(),
                invite.getReceiver().getUsername(),
                invite.getStatus(),
                invite.getMessage(),
                invite.getCreatedAt()
        );
    }
}
