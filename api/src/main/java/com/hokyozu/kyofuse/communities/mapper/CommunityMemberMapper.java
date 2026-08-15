package com.hokyozu.kyofuse.communities.mapper;

import com.hokyozu.kyofuse.communities.dto.response.CommunityMemberResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class CommunityMemberMapper {
    public static CommunityMember toEntity(User user, Community community) {
        return CommunityMember.builder()
                .community(community)
                .user(user)
                .role(CommunityMemberRole.MEMBER)
                .status(CommunityMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static CommunityMemberResponse toResponse(CommunityMember communityMember) {
        return new CommunityMemberResponse(
                communityMember.getId(),
                communityMember.getCommunity().getId(),
                communityMember.getCommunity().getName(),
                communityMember.getCommunity().getSlug(),
                communityMember.getUser().getId(),
                communityMember.getUser().getUsername(),
                communityMember.getRole(),
                communityMember.getStatus(),
                communityMember.getJoinedAt(),
                communityMember.getLeftAt(),
                communityMember.getCreatedAt(),
                communityMember.getUpdatedAt()
        );
    }
}
