package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamMemberCleanupServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private TeamMemberCleanupService cleanupService;

    @Test
    void cleanupExpiredUnassignedMembersReturnsWhenNothingExpired() {
        when(teamMemberRepository.findByMemberTypeAndAssignmentDueAtBefore(eq(TeamMemberType.UNASSIGNED), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        cleanupService.cleanupExpiredUnassignedMembers();

        verify(teamMemberRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void cleanupExpiredUnassignedMembersDeletesEachExpiredMember() {
        TeamMember first = member();
        TeamMember second = member();
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        when(teamMemberRepository.findByMemberTypeAndAssignmentDueAtBefore(eq(TeamMemberType.UNASSIGNED), instantCaptor.capture()))
                .thenReturn(List.of(first, second));

        cleanupService.cleanupExpiredUnassignedMembers();

        assertThat(instantCaptor.getValue()).isBeforeOrEqualTo(Instant.now());
        verify(teamMemberRepository).delete(first);
        verify(teamMemberRepository).delete(second);
    }

    @Test
    void cleanupExpiredUnassignedMembersContinuesWhenDeleteFails() {
        TeamMember first = member();
        TeamMember second = member();
        when(teamMemberRepository.findByMemberTypeAndAssignmentDueAtBefore(eq(TeamMemberType.UNASSIGNED), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(first, second));
        doThrow(new RuntimeException("delete failed")).when(teamMemberRepository).delete(first);

        cleanupService.cleanupExpiredUnassignedMembers();

        verify(teamMemberRepository).delete(first);
        verify(teamMemberRepository).delete(second);
    }

    private TeamMember member() {
        return TeamMember.builder()
                .team(Team.builder().id(UUID.randomUUID()).build())
                .user(User.builder().id(UUID.randomUUID()).build())
                .build();
    }
}
