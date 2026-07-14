package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.teams.dto.request.TeamMemberEditRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.teams.finder.TeamFinder;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamMemberServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private TeamFinder teamFinder;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @Spy
    private TeamChecker teamChecker = new TeamChecker();

    @InjectMocks
    private TeamMemberService teamMemberService;

    @Test
    void addMemberCreatesUnassignedMemberForTeamOwner() {
        User owner = activeUser("owner");
        User invited = activeUser("invited");
        Team team = activeTeam(owner);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(invited.getId())).thenReturn(invited);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, invited)).thenReturn(false);
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamMemberResponse response = teamMemberService.addMember(team.getId(), owner.getId(), invited.getId());

        ArgumentCaptor<TeamMember> captor = ArgumentCaptor.forClass(TeamMember.class);
        verify(teamMemberRepository).save(captor.capture());
        TeamMember saved = captor.getValue();
        assertThat(saved.getTeam()).isSameAs(team);
        assertThat(saved.getUser()).isSameAs(invited);
        assertThat(saved.getMemberType()).isEqualTo(TeamMemberType.UNASSIGNED);
        assertThat(response.teamName()).isEqualTo(team.getName());
        assertThat(response.userName()).isEqualTo(invited.getUsername());
    }

    @Test
    void addMemberRejectsExistingMember() {
        User owner = activeUser("owner");
        User invited = activeUser("invited");
        Team team = activeTeam(owner);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(invited.getId())).thenReturn(invited);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, invited)).thenReturn(true);

        assertThatThrownBy(() -> teamMemberService.addMember(team.getId(), owner.getId(), invited.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is already a member of the team.");

        verify(teamMemberRepository, never()).save(any());
    }

    @Test
    void editMemberUpdatesRoleAndClearsAssignmentDueWhenAssigned() {
        User owner = activeUser("owner");
        User edited = activeUser("edited");
        Team team = activeTeam(owner);
        TeamMember member = member(team, edited);
        member.setAssignmentDueAt(Instant.now().plusSeconds(3600));
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(edited.getId())).thenReturn(edited);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, edited)).thenReturn(true);
        when(teamMemberRepository.findByTeamAndUser(team, edited)).thenReturn(member);

        TeamMemberResponse response = teamMemberService.editMember(
                team.getId(),
                edited.getId(),
                owner.getId(),
                new TeamMemberEditRequest(PlayerRole.IGL, TeamMemberType.PLAYER)
        );

        assertThat(member.getRoleInTeam()).isEqualTo(PlayerRole.IGL);
        assertThat(member.getMemberType()).isEqualTo(TeamMemberType.PLAYER);
        assertThat(member.getAssignmentDueAt()).isNull();
        assertThat(response.roleInTeam()).isEqualTo(PlayerRole.IGL);
        verify(teamMemberRepository).save(member);
    }

    @Test
    void editMemberUpdatesRoleOnlyAndKeepsAssignmentDueWhenStillUnassigned() {
        User owner = activeUser("owner");
        User edited = activeUser("edited");
        Team team = activeTeam(owner);
        TeamMember member = member(team, edited);
        Instant assignmentDueAt = member.getAssignmentDueAt();
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(edited.getId())).thenReturn(edited);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, edited)).thenReturn(true);
        when(teamMemberRepository.findByTeamAndUser(team, edited)).thenReturn(member);

        TeamMemberResponse response = teamMemberService.editMember(
                team.getId(),
                edited.getId(),
                owner.getId(),
                new TeamMemberEditRequest(PlayerRole.IGL, null)
        );

        assertThat(response.roleInTeam()).isEqualTo(PlayerRole.IGL);
        assertThat(response.memberType()).isEqualTo(TeamMemberType.UNASSIGNED);
        assertThat(member.getAssignmentDueAt()).isEqualTo(assignmentDueAt);
        verify(teamMemberRepository).save(member);
    }

    @Test
    void editMemberUpdatesMemberTypeOnlyAndClearsAssignmentDue() {
        User owner = activeUser("owner");
        User edited = activeUser("edited");
        Team team = activeTeam(owner);
        TeamMember member = member(team, edited);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(edited.getId())).thenReturn(edited);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, edited)).thenReturn(true);
        when(teamMemberRepository.findByTeamAndUser(team, edited)).thenReturn(member);

        TeamMemberResponse response = teamMemberService.editMember(
                team.getId(),
                edited.getId(),
                owner.getId(),
                new TeamMemberEditRequest(null, TeamMemberType.SUBSTITUTE)
        );

        assertThat(response.roleInTeam()).isEqualTo(PlayerRole.SUPPORT);
        assertThat(response.memberType()).isEqualTo(TeamMemberType.SUBSTITUTE);
        assertThat(member.getAssignmentDueAt()).isNull();
        verify(teamMemberRepository).save(member);
    }

    @Test
    void editMemberRejectsEmptyRequest() {
        User owner = activeUser("owner");
        User edited = activeUser("edited");
        Team team = activeTeam(owner);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(edited.getId())).thenReturn(edited);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, edited)).thenReturn(true);

        assertThatThrownBy(() -> teamMemberService.editMember(
                team.getId(),
                edited.getId(),
                owner.getId(),
                new TeamMemberEditRequest(null, null)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("At least one field must be provided for update.");

        verify(teamMemberRepository, never()).save(any());
    }

    @Test
    void editMemberRejectsUserThatIsNotMember() {
        User owner = activeUser("owner");
        User edited = activeUser("edited");
        Team team = activeTeam(owner);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(edited.getId())).thenReturn(edited);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, edited)).thenReturn(false);

        assertThatThrownBy(() -> teamMemberService.editMember(
                team.getId(),
                edited.getId(),
                owner.getId(),
                new TeamMemberEditRequest(PlayerRole.IGL, null)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is not a member of the team.");

        verify(teamMemberRepository, never()).save(any());
    }

    @Test
    void editMemberWithNullRequestReachesMapperAndThrowsNullPointer() {
        User owner = activeUser("owner");
        User edited = activeUser("edited");
        Team team = activeTeam(owner);
        TeamMember member = member(team, edited);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(edited.getId())).thenReturn(edited);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, edited)).thenReturn(true);
        when(teamMemberRepository.findByTeamAndUser(team, edited)).thenReturn(member);

        assertThatThrownBy(() -> teamMemberService.editMember(team.getId(), edited.getId(), owner.getId(), null))
                .isInstanceOf(NullPointerException.class);

        verify(teamMemberRepository, never()).save(any());
    }

    @Test
    void listMembersMapsRepositoryPage() {
        User requester = activeUser("requester");
        Team team = activeTeam(activeUser("owner"));
        TeamMember member = member(team, requester);
        PageRequest pageable = PageRequest.of(0, 10);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(userFinder.findProfileByUserId(requester.getId())).thenReturn(requester);
        when(teamMemberRepository.findByTeam(team, pageable)).thenReturn(new PageImpl<>(List.of(member), pageable, 1));

        Page<TeamMemberResponse> response = teamMemberService.listMembers(team.getId(), requester.getId(), pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).extracting(TeamMemberResponse::userName)
                .containsExactly(requester.getUsername());
    }

    @Test
    void removeMemberDeletesExistingNonOwnerMember() {
        User owner = activeUser("owner");
        User removed = activeUser("removed");
        Team team = activeTeam(owner);
        TeamMember member = member(team, removed);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(removed.getId())).thenReturn(removed);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.findByTeamAndUser(team, removed)).thenReturn(member);

        teamMemberService.removeMember(team.getId(), owner.getId(), removed.getId());

        verify(teamMemberRepository).delete(member);
    }

    @Test
    void removeMemberRejectsRemovingOwner() {
        User owner = activeUser("owner");
        Team team = activeTeam(owner);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);

        assertThatThrownBy(() -> teamMemberService.removeMember(team.getId(), owner.getId(), owner.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Team owner cannot be removed from the team.");

        verify(teamMemberRepository, never()).delete(any());
    }

    @Test
    void removeMemberRejectsMissingMembership() {
        User owner = activeUser("owner");
        User removed = activeUser("removed");
        Team team = activeTeam(owner);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(userFinder.findProfileByUserId(removed.getId())).thenReturn(removed);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);

        assertThatThrownBy(() -> teamMemberService.removeMember(team.getId(), owner.getId(), removed.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Member does not exist in this team.");

        verify(teamMemberRepository, never()).delete(any());
    }

    @Test
    void leaveTeamDeletesMembershipForNonOwner() {
        User owner = activeUser("owner");
        User memberUser = activeUser("member");
        Team team = activeTeam(owner);
        TeamMember member = member(team, memberUser);
        when(userFinder.findProfileByUserId(memberUser.getId())).thenReturn(memberUser);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, memberUser)).thenReturn(true);
        when(teamMemberRepository.findByTeamAndUser(team, memberUser)).thenReturn(member);

        teamMemberService.leaveTeam(team.getId(), memberUser.getId());

        verify(teamMemberRepository).delete(member);
    }

    @Test
    void leaveTeamRejectsOwner() {
        User owner = activeUser("owner");
        Team team = activeTeam(owner);
        when(userFinder.findProfileByUserId(owner.getId())).thenReturn(owner);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);

        assertThatThrownBy(() -> teamMemberService.leaveTeam(team.getId(), owner.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Team owner cannot leave the team.");
    }

    @Test
    void leaveTeamRejectsUserThatIsNotMember() {
        User owner = activeUser("owner");
        User memberUser = activeUser("member");
        Team team = activeTeam(owner);
        when(userFinder.findProfileByUserId(memberUser.getId())).thenReturn(memberUser);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, memberUser)).thenReturn(false);

        assertThatThrownBy(() -> teamMemberService.leaveTeam(team.getId(), memberUser.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is not a member of the team.");

        verify(teamMemberRepository, never()).delete(any());
    }

    @Test
    void leaveTeamRejectsMissingMembershipAfterExistsCheck() {
        User owner = activeUser("owner");
        User memberUser = activeUser("member");
        Team team = activeTeam(owner);
        when(userFinder.findProfileByUserId(memberUser.getId())).thenReturn(memberUser);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(teamMemberRepository.existsByTeamAndUser(team, memberUser)).thenReturn(true);

        assertThatThrownBy(() -> teamMemberService.leaveTeam(team.getId(), memberUser.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Member does not exist in this team.");

        verify(teamMemberRepository, never()).delete(any());
    }

    @Test
    void detailMemberReturnsExistingMember() {
        User requester = activeUser("requester");
        User memberUser = activeUser("member");
        Team team = activeTeam(activeUser("owner"));
        TeamMember member = member(team, memberUser);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(userFinder.findProfileByUserId(requester.getId())).thenReturn(requester);
        when(userFinder.findProfileByUserId(memberUser.getId())).thenReturn(memberUser);
        when(teamMemberRepository.findByTeamAndUser(team, memberUser)).thenReturn(member);

        TeamMemberResponse response = teamMemberService.detailMember(team.getId(), requester.getId(), memberUser.getId());

        assertThat(response.userName()).isEqualTo(memberUser.getUsername());
    }

    @Test
    void detailMemberRejectsMissingMember() {
        User requester = activeUser("requester");
        User memberUser = activeUser("member");
        Team team = activeTeam(activeUser("owner"));
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(userFinder.findProfileByUserId(requester.getId())).thenReturn(requester);
        when(userFinder.findProfileByUserId(memberUser.getId())).thenReturn(memberUser);

        assertThatThrownBy(() -> teamMemberService.detailMember(team.getId(), requester.getId(), memberUser.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Team member not found.");
    }

    @Test
    void detailMemberRejectsMemberFromAnotherTeam() {
        User requester = activeUser("requester");
        User memberUser = activeUser("member");
        Team team = activeTeam(activeUser("owner"));
        Team otherTeam = activeTeam(activeUser("other-owner"));
        TeamMember member = member(otherTeam, memberUser);
        when(teamFinder.findTeamById(team.getId())).thenReturn(team);
        when(userFinder.findProfileByUserId(requester.getId())).thenReturn(requester);
        when(userFinder.findProfileByUserId(memberUser.getId())).thenReturn(memberUser);
        when(teamMemberRepository.findByTeamAndUser(team, memberUser)).thenReturn(member);

        assertThatThrownBy(() -> teamMemberService.detailMember(team.getId(), requester.getId(), memberUser.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Team member does not belong to the specified team.");
    }

    private TeamMember member(Team team, User user) {
        Instant now = Instant.now();
        return TeamMember.builder()
                .team(team)
                .user(user)
                .roleInTeam(PlayerRole.SUPPORT)
                .memberType(TeamMemberType.UNASSIGNED)
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(now)
                .assignmentDueAt(now.plusSeconds(3600))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private Team activeTeam(User owner) {
        return Team.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .name("Kyofuse Academy")
                .status(TeamStatus.ACTIVE)
                .build();
    }

    private User activeUser(String username) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
