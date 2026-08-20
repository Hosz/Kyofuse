package com.hokyozu.kyofuse.teams.mapper;

import com.hokyozu.kyofuse.teams.dto.request.TeamMemberEditRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TeamMemberMapper {
    /**
     * Dono do time: entra como MANAGER e sem prazo de definição. UNASSIGNED com
     * assignmentDueAt faria o TeamMemberCleanupService remover o próprio dono do time
     * depois de 14 dias.
     */
    public static TeamMember toOwnerEntity(User owner, Team team) {
        return TeamMember.builder()
                .team(team)
                .user(owner)
                .roleInTeam(null)
                .memberType(TeamMemberType.MANAGER)
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .leftAt(null)
                .assignmentDueAt(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static TeamMember toEntity(User userInvited, Team team) {
        return TeamMember.builder()
                .team(team)
                .user(userInvited)
                .roleInTeam(null)
                .memberType(TeamMemberType.UNASSIGNED)
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .leftAt(null)
                .assignmentDueAt(Instant.now().plus(14, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static TeamMemberResponse toResponse(TeamMember savedTeamMember) {
        return toResponse(savedTeamMember, null);
    }

    /** profile opcional: quando vem preenchido, a listagem mostra apelido e foto. */
    public static TeamMemberResponse toResponse(TeamMember savedTeamMember, GamerProfile profile) {
        return new TeamMemberResponse(
                savedTeamMember.getTeam().getName(),
                savedTeamMember.getUser().getId(),
                savedTeamMember.getUser().getUsername(),
                profile != null ? profile.getNickname() : null,
                profile != null ? profile.getAvatarUrl() : null,
                savedTeamMember.getRoleInTeam(),
                savedTeamMember.getMemberType(),
                savedTeamMember.getStatus(),
                savedTeamMember.getJoinedAt(),
                savedTeamMember.getAssignmentDueAt(),
                savedTeamMember.getCreatedAt(),
                savedTeamMember.getUpdatedAt()
        );
    }

    public static void toUpdate(TeamMember teamMember, TeamMemberEditRequest request) {

        if (request.roleInTeam() != null) {
            teamMember.setRoleInTeam(request.roleInTeam());
        }

        if (request.memberType() != null) {
            teamMember.setMemberType(request.memberType());
        }

        teamMember.setUpdatedAt(Instant.now());
    }
}
