package com.hokyozu.kyofuse.teams.mapper;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.dto.request.TeamMemberEditRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TeamMemberMapperTest {

    @Test
    void canInstantiateMapper() {
        assertThat(new TeamMemberMapper()).isNotNull();
    }

    @Test
    void toEntityCreatesActiveUnassignedMemberWithAssignmentDueDate() {
        User user = user("member");
        Team team = team("Academy");
        Instant before = Instant.now();

        TeamMember member = TeamMemberMapper.toEntity(user, team);

        Instant after = Instant.now();
        assertThat(member.getTeam()).isSameAs(team);
        assertThat(member.getUser()).isSameAs(user);
        assertThat(member.getRoleInTeam()).isNull();
        assertThat(member.getMemberType()).isEqualTo(TeamMemberType.UNASSIGNED);
        assertThat(member.getStatus()).isEqualTo(TeamMemberStatus.ACTIVE);
        assertThat(member.getLeftAt()).isNull();
        assertThat(member.getJoinedAt()).isBetween(before, after);
        assertThat(member.getCreatedAt()).isBetween(before, after);
        assertThat(member.getUpdatedAt()).isBetween(before, after);
        assertThat(member.getAssignmentDueAt())
                .isBetween(before.plus(14, ChronoUnit.DAYS), after.plus(14, ChronoUnit.DAYS));
    }

    @Test
    void toManagerEntityCreatesActiveManagerWithoutAssignmentDueDate() {
        User user = user("manager");
        Team team = team("Academy");
        Instant before = Instant.now();

        TeamMember member = TeamMemberMapper.toManagerEntity(user, team);

        Instant after = Instant.now();
        assertThat(member.getTeam()).isSameAs(team);
        assertThat(member.getUser()).isSameAs(user);
        assertThat(member.getRoleInTeam()).isNull();
        assertThat(member.getMemberType()).isEqualTo(TeamMemberType.MANAGER);
        assertThat(member.getStatus()).isEqualTo(TeamMemberStatus.ACTIVE);
        assertThat(member.getLeftAt()).isNull();
        assertThat(member.getAssignmentDueAt()).isNull();
        assertThat(member.getJoinedAt()).isBetween(before, after);
        assertThat(member.getCreatedAt()).isBetween(before, after);
        assertThat(member.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void toResponseMapsTeamMemberFields() {
        Instant now = Instant.now();
        TeamMember member = TeamMember.builder()
                .team(team("Academy"))
                .user(user("player"))
                .roleInTeam(PlayerRole.AWPER)
                .memberType(TeamMemberType.PLAYER)
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(now)
                .assignmentDueAt(now.plus(1, ChronoUnit.DAYS))
                .createdAt(now.minusSeconds(2))
                .updatedAt(now.minusSeconds(1))
                .build();

        TeamMemberResponse response = TeamMemberMapper.toResponse(member);

        assertThat(response.teamName()).isEqualTo("Academy");
        assertThat(response.userName()).isEqualTo("player");
        assertThat(response.roleInTeam()).isEqualTo(PlayerRole.AWPER);
        assertThat(response.memberType()).isEqualTo(TeamMemberType.PLAYER);
        assertThat(response.status()).isEqualTo(TeamMemberStatus.ACTIVE);
        assertThat(response.joinedAt()).isEqualTo(now);
        assertThat(response.assignmentDueAt()).isEqualTo(now.plus(1, ChronoUnit.DAYS));
        assertThat(response.createdAt()).isEqualTo(now.minusSeconds(2));
        assertThat(response.updatedAt()).isEqualTo(now.minusSeconds(1));
    }

    @Test
    void toUpdateChangesOnlyProvidedFields() {
        TeamMember member = TeamMember.builder()
                .roleInTeam(PlayerRole.SUPPORT)
                .memberType(TeamMemberType.UNASSIGNED)
                .updatedAt(Instant.now().minusSeconds(60))
                .build();

        TeamMemberMapper.toUpdate(member, new TeamMemberEditRequest(PlayerRole.RIFLER, null));

        assertThat(member.getRoleInTeam()).isEqualTo(PlayerRole.RIFLER);
        assertThat(member.getMemberType()).isEqualTo(TeamMemberType.UNASSIGNED);
        assertThat(member.getUpdatedAt()).isAfter(Instant.now().minusSeconds(10));

        TeamMemberMapper.toUpdate(member, new TeamMemberEditRequest(null, TeamMemberType.COACH));

        assertThat(member.getRoleInTeam()).isEqualTo(PlayerRole.RIFLER);
        assertThat(member.getMemberType()).isEqualTo(TeamMemberType.COACH);
    }

    private Team team(String name) {
        return Team.builder()
                .id(UUID.randomUUID())
                .name(name)
                .build();
    }

    private User user(String username) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .build();
    }
}
