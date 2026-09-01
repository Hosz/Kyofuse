package com.hokyozu.kyofuse.teams.controller;

import com.hokyozu.kyofuse.teams.dto.request.TeamFilter;
import com.hokyozu.kyofuse.teams.dto.request.TeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.teams.service.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamController controller;

    @Test
    void createTeamsUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        TeamRequest request = request();
        TeamResponse expected = response();
        when(teamService.createTeams(request, userId)).thenReturn(expected);

        TeamResponse result = controller.createTeams(jwt(userId), request);

        assertThat(result).isSameAs(expected);
        verify(teamService).createTeams(request, userId);
    }

    @Test
    void detailTeamUsesPathTeamId() {
        String teamId = "kyofuse";
        TeamResponse expected = response();
        when(teamService.detailTeam(teamId)).thenReturn(expected);

        TeamResponse result = controller.detailTeam(teamId);

        assertThat(result).isSameAs(expected);
        verify(teamService).detailTeam(teamId);
    }

    @Test
    void listingTeamsDelegatesFilterAndPageable() {
        TeamFilter filter = new TeamFilter(null, null, TeamStatus.ACTIVE, null, null, null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20);
        Page<TeamResponse> expected = new PageImpl<>(List.of(response()));
        when(teamService.listingTeams(filter, pageable)).thenReturn(expected);

        Page<TeamResponse> result = controller.listingTeams(jwt(UUID.randomUUID()), filter, pageable);

        assertThat(result).isSameAs(expected);
        verify(teamService).listingTeams(filter, pageable);
    }

    @Test
    void editTeamsUsesAuthenticatedUserIdAndPathTeamId() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UpdateTeamRequest request = new UpdateTeamRequest("Edited name", "New description", null, null, null, null, null, null, null, null, null, null);
        TeamResponse expected = response();
        when(teamService.editTeam(userId, teamId, request)).thenReturn(expected);

        TeamResponse result = controller.editTeam(jwt(userId), teamId, request);

        assertThat(result).isSameAs(expected);
        verify(teamService).editTeam(userId, teamId, request);
    }

    @Test
    void inactiveTeamUsesAuthenticatedUserIdAndPathTeamId() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        TeamResponse expected = response();
        when(teamService.inactiveTeam(userId, teamId)).thenReturn(expected);

        TeamResponse result = controller.inactiveTeam(jwt(userId), teamId);

        assertThat(result).isSameAs(expected);
        verify(teamService).inactiveTeam(userId, teamId);
    }

    private TeamRequest request() {
        return new TeamRequest(
                "Kyofuse Team",
                null,
                null,
                "kyofuse-team",
                "Team description",
                "BR",
                10000,
                20000,
                5,
                10,
                10,
                20,
                List.of()
        );
    }

    private TeamResponse response() {
        Instant now = Instant.now();
        return new TeamResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "owner",
                "Kyofuse Team",
                "kyofuse-team",
                "Team description",
                null,
                null,
                "BR",
                10000,
                20000,
                5,
                10,
                10,
                20,
                TeamStatus.ACTIVE,
                List.of(),
                now,
                now
        );
    }

    private Jwt jwt(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
