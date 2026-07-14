package com.hokyozu.kyofuse.teams.scheduler;

import com.hokyozu.kyofuse.teams.service.TeamMemberCleanupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeamMemberCleanupSchedulerTest {

    @Mock
    private TeamMemberCleanupService cleanupService;

    @InjectMocks
    private TeamMemberCleanupScheduler scheduler;

    @Test
    void scheduleTeamMemberCleanupRunsCleanupService() {
        scheduler.scheduleTeamMemberCleanup();

        verify(cleanupService).cleanupExpiredUnassignedMembers();
    }

    @Test
    void scheduleTeamMemberCleanupSwallowsCleanupExceptions() {
        doThrow(new RuntimeException("failed")).when(cleanupService).cleanupExpiredUnassignedMembers();

        scheduler.scheduleTeamMemberCleanup();

        verify(cleanupService).cleanupExpiredUnassignedMembers();
    }
}
