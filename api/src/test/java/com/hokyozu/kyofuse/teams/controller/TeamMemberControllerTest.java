package com.hokyozu.kyofuse.teams.controller;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.dto.request.TeamMemberEditRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.service.TeamMemberService;
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
class TeamMemberControllerTest {

    @Mock
    private TeamMemberService teamMemberService;

    @InjectMocks
    private TeamMemberController controller;

    @Test
    void addMemberUsesAuthenticatedUserIdAndPathIds() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID invitedId = UUID.randomUUID();
        TeamMemberResponse expected = response();
        when(teamMemberService.addMember(teamId, userId, invitedId)).thenReturn(expected);

        TeamMemberResponse result = controller.addMember(teamId, jwt(userId), invitedId);

        assertThat(result).isSameAs(expected);
        verify(teamMemberService).addMember(teamId, userId, invitedId);
    }

    @Test
    void editMemberUsesAuthenticatedUserIdPathIdsAndRequest() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID editedId = UUID.randomUUID();
        TeamMemberEditRequest request = new TeamMemberEditRequest(PlayerRole.RIFLER, TeamMemberType.PLAYER);
        TeamMemberResponse expected = response();
        when(teamMemberService.editMember(teamId, editedId, userId, request)).thenReturn(expected);

        TeamMemberResponse result = controller.editMember(teamId, editedId, jwt(userId), request);

        assertThat(result).isSameAs(expected);
        verify(teamMemberService).editMember(teamId, editedId, userId, request);
    }

    @Test
    void listMembersUsesAuthenticatedUserIdAndPageable() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<TeamMemberResponse> expected = new PageImpl<>(List.of(response()));
        when(teamMemberService.listMembers(teamId, userId, pageable)).thenReturn(expected);

        Page<TeamMemberResponse> result = controller.listMembers(teamId, jwt(userId), pageable);

        assertThat(result).isSameAs(expected);
        verify(teamMemberService).listMembers(teamId, userId, pageable);
    }

    @Test
    void detailMemberUsesAuthenticatedUserIdAndPathIds() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        TeamMemberResponse expected = response();
        when(teamMemberService.detailMember(teamId, userId, memberId)).thenReturn(expected);

        TeamMemberResponse result = controller.detailMember(teamId, memberId, jwt(userId));

        assertThat(result).isSameAs(expected);
        verify(teamMemberService).detailMember(teamId, userId, memberId);
    }

    @Test
    void removeMemberUsesAuthenticatedUserIdAndPathIds() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID removedId = UUID.randomUUID();

        controller.removeMember(teamId, removedId, jwt(userId));

        verify(teamMemberService).removeMember(teamId, userId, removedId);
    }

    @Test
    void leaveTeamUsesAuthenticatedUserIdAndPathTeamId() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        controller.leaveTeam(teamId, jwt(userId));

        verify(teamMemberService).leaveTeam(teamId, userId);
    }

    private TeamMemberResponse response() {
        Instant now = Instant.now();
        UUID teamId = UUID.randomUUID();
        return new TeamMemberResponse(
                "",
                teamId,
                "Kyofuse",
                PlayerRole.AWPER,
                TeamMemberType.PLAYER,
                TeamMemberStatus.ACTIVE,
                now,
                null,
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
