package com.hokyozu.kyofuse.communities.mapper;

import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;

import java.time.Instant;

public class CommunityMapper {
    public static Community toEntity(@Valid CommunityRequest request, User user) {
        return Community.builder()
                .owner(user)
                .team(null)
                .name(request.communityName())
                .slug(request.communitySlug())
                .description(request.communityDescription())
                .avatarUrl(request.communityAvatarUrl())
                .bannerUrl(request.communityBannerUrl())
                .visibility(request.visibility())
                .status(CommunityStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static CommunityResponse toResponse(Community community) {
        return new CommunityResponse(
                community.getId(),
                community.getName(),
                community.getSlug(),
                community.getDescription(),
                community.getAvatarUrl() != null ? community.getAvatarUrl() : "/assets/profile/community-profile-image-default.png",
                community.getBannerUrl(),
                community.getOwner().getId(),
                community.getOwner().getUsername(),
                community.getTeam() != null ? community.getTeam().getId() : null,
                community.getTeam() != null ? community.getTeam().getSlug() : null,
                community.getTeam() != null ? community.getTeam().getName() : null,
                community.getTeam() != null ? (community.getTeam().getAvatarUrl() != null ? community.getTeam().getAvatarUrl() : "/assets/profile/team-profile-image-default.png") : null,
                community.getVisibility(),
                community.getStatus(),
                //community.getMemberCount(),
                //community.getPostCount(),
                community.getCreatedAt(),
                community.getUpdatedAt()
        );
    }

    public static void toEdit(Community community, @Valid UpdateCommunityRequest request) {
        if (request.communityName() != null) {
            community.setName(request.communityName());
        }

        if (request.communitySlug() != null) {
            community.setSlug(request.communitySlug());
        }

        if (request.communityDescription() != null) {
            community.setDescription(request.communityDescription());
        }

        if (request.communityAvatarUrl() != null) {
            community.setAvatarUrl(request.communityAvatarUrl().trim().isEmpty() ? null : request.communityAvatarUrl().trim());
        }

        if (request.communityBannerUrl() != null) {
            community.setBannerUrl(request.communityBannerUrl().trim().isEmpty() ? null : request.communityBannerUrl().trim());
        }

        if (request.visibility() != null) {
            community.setVisibility(request.visibility());
        }

        community.setUpdatedAt(Instant.now());
    }

    public static Community toEntityTeamCommunity(User user, Team team, String slug) {
        return Community.builder()
                .owner(user)
                .team(team)
                .name(team.getName())
                .slug(slug)
                .description(team.getDescription())
                .avatarUrl(team.getAvatarUrl())
                .bannerUrl(team.getBannerUrl())
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
