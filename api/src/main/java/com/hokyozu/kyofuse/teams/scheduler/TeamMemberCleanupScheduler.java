package com.hokyozu.kyofuse.teams.scheduler;

import com.hokyozu.kyofuse.teams.service.TeamMemberCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TeamMemberCleanupScheduler {

    private final TeamMemberCleanupService teamMemberCleanupService;

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduleTeamMemberCleanup() {
        log.info("Team Member Cleanup Scheduler triggered at 2:00 AM");
        try {
            teamMemberCleanupService.cleanupExpiredUnassignedMembers();
        } catch (Exception e) {
            log.error("Error during team member cleanup", e);
        }
    }
}
