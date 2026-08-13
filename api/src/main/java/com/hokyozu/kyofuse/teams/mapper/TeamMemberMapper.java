package com.hokyozu.kyofuse.teams.mapper;

import com.hokyozu.kyofuse.teams.dto.request.TeamMemberEditRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TeamMemberMapper {
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
        return new TeamMemberResponse(
                savedTeamMember.getTeam().getName(),
                savedTeamMember.getUser().getId(),
                savedTeamMember.getUser().getUsername(),
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
