package com.hokyozu.kyofuse.users.service;

import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountSuccessionServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private CommunityMemberRepository communityMemberRepository;
    @Mock
    private com.hokyozu.kyofuse.users.repository.AccountSuccessionRecordRepository accountSuccessionRecordRepository;

    @InjectMocks
    private AccountSuccessionService successionService;

    private User owner;
    private User manager;
    private User player;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        manager = User.builder().id(UUID.randomUUID()).username("manager").build();
        player = User.builder().id(UUID.randomUUID()).username("player").build();
    }

    @Test
    @DisplayName("Transfers team leadership to manager and demotes owner")
    void transfersTeamLeadershipToManager() {
        Team team = Team.builder().id(UUID.randomUUID()).name("Team Elite").owner(owner).status(TeamStatus.ACTIVE).build();
        TeamMember ownerMember = TeamMember.builder().user(owner).memberType(TeamMemberType.MANAGER).status(TeamMemberStatus.ACTIVE).joinedAt(Instant.now().minusSeconds(1000)).build();
        TeamMember managerMember = TeamMember.builder().user(manager).memberType(TeamMemberType.MANAGER).status(TeamMemberStatus.ACTIVE).joinedAt(Instant.now().minusSeconds(500)).build();
        TeamMember playerMember = TeamMember.builder().user(player).memberType(TeamMemberType.PLAYER).status(TeamMemberStatus.ACTIVE).joinedAt(Instant.now().minusSeconds(200)).build();

        when(teamRepository.findAllByOwner(owner)).thenReturn(List.of(team));
        when(teamMemberRepository.findByTeam(team)).thenReturn(List.of(ownerMember, managerMember, playerMember));
        when(communityRepository.findAllByOwner(owner)).thenReturn(List.of());

        successionService.handleOwnershipTransferAndDemotion(owner);

        assertEquals(manager, team.getOwner());
        assertEquals(TeamMemberType.UNASSIGNED, ownerMember.getMemberType());
        assertNull(ownerMember.getRoleInTeam());
        verify(teamRepository).save(team);
        verify(teamMemberRepository).save(managerMember);
        verify(teamMemberRepository).save(ownerMember);
    }

    @Test
    @DisplayName("Transfers community leadership to oldest admin and demotes owner")
    void transfersCommunityLeadershipToAdmin() {
        Community community = Community.builder().id(UUID.randomUUID()).name("CS2 Pro").owner(owner).status(CommunityStatus.ACTIVE).build();
        CommunityMember ownerMember = CommunityMember.builder().user(owner).role(CommunityMemberRole.ADMIN).status(CommunityMemberStatus.ACTIVE).joinedAt(Instant.now().minusSeconds(1000)).build();
        CommunityMember adminMember = CommunityMember.builder().user(manager).role(CommunityMemberRole.ADMIN).status(CommunityMemberStatus.ACTIVE).joinedAt(Instant.now().minusSeconds(600)).build();

        when(teamRepository.findAllByOwner(owner)).thenReturn(List.of());
        when(communityRepository.findAllByOwner(owner)).thenReturn(List.of(community));
        when(communityMemberRepository.findByCommunityAndStatus(community, CommunityMemberStatus.ACTIVE)).thenReturn(List.of(ownerMember, adminMember));

        successionService.handleOwnershipTransferAndDemotion(owner);

        assertEquals(manager, community.getOwner());
        assertEquals(CommunityMemberRole.MEMBER, ownerMember.getRole());
        verify(communityRepository).save(community);
        verify(communityMemberRepository).save(adminMember);
        verify(communityMemberRepository).save(ownerMember);
    }

    @Test
    @DisplayName("Reactivates inactive teams and sets owner member type to manager")
    void reactivatesInactiveTeams() {
        Team team = Team.builder().id(UUID.randomUUID()).name("Solo Team").owner(owner).status(TeamStatus.INACTIVE).build();
        TeamMember ownerMember = TeamMember.builder().user(owner).memberType(TeamMemberType.UNASSIGNED).status(TeamMemberStatus.ACTIVE).build();
        Community linkedComm = Community.builder().id(UUID.randomUUID()).name("Linked Comm").owner(owner).status(CommunityStatus.ARCHIVED).build();

        when(teamRepository.findAllByOwner(owner)).thenReturn(List.of(team));
        when(teamMemberRepository.findByTeam(team)).thenReturn(List.of(ownerMember));
        when(communityRepository.findByTeamId(team.getId())).thenReturn(Optional.of(linkedComm));
        when(communityRepository.findAllByOwner(owner)).thenReturn(List.of());

        successionService.handleAccountReactivation(owner);

        assertEquals(TeamStatus.ACTIVE, team.getStatus());
        assertEquals(TeamMemberType.MANAGER, ownerMember.getMemberType());
        assertEquals(CommunityStatus.ACTIVE, linkedComm.getStatus());
        verify(teamRepository).save(team);
        verify(teamMemberRepository).save(ownerMember);
        verify(communityRepository).save(linkedComm);
    }

    @Test
    @DisplayName("Reactivates archived community and sets owner role to admin")
    void reactivatesArchivedCommunities() {
        Community community = Community.builder().id(UUID.randomUUID()).name("Solo Community").owner(owner).status(CommunityStatus.ARCHIVED).build();
        CommunityMember ownerMember = CommunityMember.builder().user(owner).role(CommunityMemberRole.MEMBER).status(CommunityMemberStatus.ACTIVE).build();

        when(teamRepository.findAllByOwner(owner)).thenReturn(List.of());
        when(communityRepository.findAllByOwner(owner)).thenReturn(List.of(community));
        when(communityMemberRepository.findByCommunityAndUser(community, owner)).thenReturn(Optional.of(ownerMember));

        successionService.handleAccountReactivation(owner);

        assertEquals(CommunityStatus.ACTIVE, community.getStatus());
        assertEquals(CommunityMemberRole.ADMIN, ownerMember.getRole());
        verify(communityRepository).save(community);
        verify(communityMemberRepository).save(ownerMember);
    }

    @Test
    @DisplayName("Restores transferred team ownership to original owner and reverts successor to previous member type")
    void restoresTransferredTeamOwnership() {
        Team team = Team.builder().id(UUID.randomUUID()).name("Team Elite").owner(manager).status(TeamStatus.ACTIVE).build();
        TeamMember ownerMember = TeamMember.builder().user(owner).memberType(TeamMemberType.UNASSIGNED).status(TeamMemberStatus.ACTIVE).build();
        TeamMember managerMember = TeamMember.builder().user(manager).memberType(TeamMemberType.MANAGER).status(TeamMemberStatus.ACTIVE).build();

        com.hokyozu.kyofuse.users.entity.AccountSuccessionRecord record = com.hokyozu.kyofuse.users.entity.AccountSuccessionRecord.builder()
                .id(UUID.randomUUID())
                .user(owner)
                .entityType(com.hokyozu.kyofuse.users.enums.SuccessionEntityType.TEAM)
                .entityId(team.getId())
                .successor(manager)
                .previousMemberType("MANAGER")
                .previousRoleInTeam("IGL")
                .previousSuccessorMemberType("PLAYER")
                .wasOwner(true)
                .previousStatus("ACTIVE")
                .createdAt(Instant.now().minusSeconds(100))
                .build();

        when(accountSuccessionRecordRepository.findAllByUser(owner)).thenReturn(List.of(record));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(teamMemberRepository.findByTeamAndUser(team, manager)).thenReturn(managerMember);
        when(teamMemberRepository.findByTeamAndUser(team, owner)).thenReturn(ownerMember);
        when(communityRepository.findByTeamId(team.getId())).thenReturn(Optional.empty());

        successionService.handleAccountReactivation(owner);

        assertEquals(owner, team.getOwner());
        assertEquals(TeamMemberType.MANAGER, ownerMember.getMemberType());
        assertEquals(com.hokyozu.kyofuse.profiles.enums.PlayerRole.IGL, ownerMember.getRoleInTeam());
        assertEquals(TeamMemberType.PLAYER, managerMember.getMemberType());
        verify(teamRepository).save(team);
        verify(teamMemberRepository).save(managerMember);
        verify(teamMemberRepository).save(ownerMember);
        verify(accountSuccessionRecordRepository).deleteAllByUser(owner);
    }

    @Test
    @DisplayName("Restores transferred community ownership to original owner and reverts successor role")
    void restoresTransferredCommunityOwnership() {
        Community community = Community.builder().id(UUID.randomUUID()).name("CS2 Pro").owner(manager).status(CommunityStatus.ACTIVE).build();
        CommunityMember ownerMember = CommunityMember.builder().user(owner).role(CommunityMemberRole.MEMBER).status(CommunityMemberStatus.ACTIVE).build();
        CommunityMember managerMember = CommunityMember.builder().user(manager).role(CommunityMemberRole.ADMIN).status(CommunityMemberStatus.ACTIVE).build();

        com.hokyozu.kyofuse.users.entity.AccountSuccessionRecord record = com.hokyozu.kyofuse.users.entity.AccountSuccessionRecord.builder()
                .id(UUID.randomUUID())
                .user(owner)
                .entityType(com.hokyozu.kyofuse.users.enums.SuccessionEntityType.COMMUNITY)
                .entityId(community.getId())
                .successor(manager)
                .previousRole("ADMIN")
                .previousSuccessorRole("MODERATOR")
                .wasOwner(true)
                .previousStatus("ACTIVE")
                .createdAt(Instant.now().minusSeconds(100))
                .build();

        when(accountSuccessionRecordRepository.findAllByUser(owner)).thenReturn(List.of(record));
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByCommunityAndUser(community, manager)).thenReturn(Optional.of(managerMember));
        when(communityMemberRepository.findByCommunityAndUser(community, owner)).thenReturn(Optional.of(ownerMember));

        successionService.handleAccountReactivation(owner);

        assertEquals(owner, community.getOwner());
        assertEquals(CommunityMemberRole.ADMIN, ownerMember.getRole());
        assertEquals(CommunityMemberRole.MODERATOR, managerMember.getRole());
        verify(communityRepository).save(community);
        verify(communityMemberRepository).save(managerMember);
        verify(communityMemberRepository).save(ownerMember);
        verify(accountSuccessionRecordRepository).deleteAllByUser(owner);
    }
}
