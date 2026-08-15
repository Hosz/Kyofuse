package com.hokyozu.kyofuse.communities.mapper;

import com.hokyozu.kyofuse.communities.dto.response.CommunityJoinRequestResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityJoinRequest;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class CommunityJoinRequestMapper {
    public static CommunityJoinRequest toEntity(User user, Community community) {
        return CommunityJoinRequest.builder()
                .community(community)
                .requester(user)
                .createdAt(Instant.now())
                .build();
    }

    public static CommunityJoinRequestResponse toResponse(CommunityJoinRequest communityJoinRequest) {
        return new CommunityJoinRequestResponse(
                communityJoinRequest.getId(),
                communityJoinRequest.getCommunity().getId(),
                communityJoinRequest.getCommunity().getName(),
                communityJoinRequest.getCommunity().getSlug(),
                communityJoinRequest.getRequester().getId(),
                communityJoinRequest.getRequester().getUsername(),
                communityJoinRequest.getCreatedAt()
        );
    }
}
