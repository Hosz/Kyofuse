package com.hokyozu.kyofuse.teams.mapper;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.dto.request.TeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;

public class TeamMapper {
    public static Team toEntity(@Valid TeamRequest request, User user) {
        return Team.builder()
                .owner(user)
                .name(request.name())
                .slug(request.slug())
                .avatarUrl(request.avatarUrl())
                .bannerUrl(request.bannerUrl())
                .description(request.description())
                .region(request.region())
                .minPremierRating(request.minPremierRating())
                .maxPremierRating(request.maxPremierRating())
                .minFaceitLevel(request.minFaceitLevel())
                .maxFaceitLevel(request.maxFaceitLevel())
                .minGcRank(request.minGcRank())
                .maxGcRank(request.maxGcRank())
                .status(TeamStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static TeamResponse toResponse(Team teamSaved, List<TeamRequiredRole> requiredRolesSaved) {
        List<PlayerRole> roles = requiredRolesSaved.stream()
                .map(TeamRequiredRole::getRoleName)
                .toList();

        return new TeamResponse(
                teamSaved.getId(),
                teamSaved.getOwner().getUsername(),
                teamSaved.getName(),
                teamSaved.getSlug(),
                teamSaved.getAvatarUrl(),
                teamSaved.getBannerUrl(),
                teamSaved.getDescription(),
                teamSaved.getRegion(),
                teamSaved.getMinPremierRating(),
                teamSaved.getMaxPremierRating(),
                teamSaved.getMinFaceitLevel(),
                teamSaved.getMaxFaceitLevel(),
                teamSaved.getMinGcRank(),
                teamSaved.getMaxGcRank(),
                teamSaved.getStatus(),
                roles,
                teamSaved.getCreatedAt(),
                teamSaved.getUpdatedAt()
        );
    }

    public static void toUpdate(Team team, UpdateTeamRequest updateTeamRequest) {

        if (updateTeamRequest.name() != null) {
            team.setName(updateTeamRequest.name());
        }

        if (updateTeamRequest.description() != null) {
            team.setDescription(updateTeamRequest.description());
        } else if (updateTeamRequest.description() == null && team.getDescription() == null) {
            team.setDescription(null);
        }

        if (updateTeamRequest.avatarUrl() != null) {
            team.setAvatarUrl(updateTeamRequest.avatarUrl());
        } else if (updateTeamRequest.avatarUrl() == null && team.getAvatarUrl() == null) {
            team.setAvatarUrl(null);
        }

        if (updateTeamRequest.bannerUrl() != null) {
            team.setBannerUrl(updateTeamRequest.bannerUrl());
        } else if (updateTeamRequest.bannerUrl() == null && team.getBannerUrl() == null) {
            team.setBannerUrl(null);
        }

        if (updateTeamRequest.region() != null) {
            team.setRegion(updateTeamRequest.region());
        } else if (updateTeamRequest.region() == null && team.getRegion() == null) {
            team.setRegion(null);
        }

        if (updateTeamRequest.minPremierRating() != null) {
            team.setMinPremierRating(updateTeamRequest.minPremierRating());
        } else {
            team.setMinPremierRating(null);
        }

        if (updateTeamRequest.maxPremierRating() != null) {
            team.setMaxPremierRating(updateTeamRequest.maxPremierRating());
        } else {
            team.setMaxPremierRating(null);
        }

        if (updateTeamRequest.minFaceitLevel() != null) {
            team.setMinFaceitLevel(updateTeamRequest.minFaceitLevel());
        } else {
            team.setMinFaceitLevel(null);
        }

        if (updateTeamRequest.maxFaceitLevel() != null) {
            team.setMaxFaceitLevel(updateTeamRequest.maxFaceitLevel());
        } else {
            team.setMaxFaceitLevel(null);
        }

        if (updateTeamRequest.minGcRank() != null) {
            team.setMinGcRank(updateTeamRequest.minGcRank());
        } else {
            team.setMinGcRank(null);
        }

        if (updateTeamRequest.maxGcRank() != null) {
            team.setMaxGcRank(updateTeamRequest.maxGcRank());
        } else {
            team.setMaxGcRank(null);
        }

        team.setUpdatedAt(Instant.now());
    }
}
