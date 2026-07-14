package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamMemberCleanupService {

    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public void cleanupExpiredUnassignedMembers() {
        log.info("Starting cleanup of expired unassigned team members");

        Instant now = Instant.now();
        
        List<TeamMember> expiredMembers = teamMemberRepository.findByMemberTypeAndAssignmentDueAtBefore(
                TeamMemberType.UNASSIGNED,
                now
        );

        if (expiredMembers.isEmpty()) {
            log.info("No expired unassigned members found for cleanup");
            return;
        }

        log.info("Found {} expired unassigned members for cleanup", expiredMembers.size());

        for (TeamMember member : expiredMembers) {
            try {
                log.info("Removing expired unassigned member: userId={}, teamId={}", 
                        member.getUser().getId(), member.getTeam().getId());
                teamMemberRepository.delete(member);
            } catch (Exception e) {
                log.error("Error deleting expired unassigned member: userId={}, teamId={}", 
                        member.getUser().getId(), member.getTeam().getId(), e);
            }
        }

        log.info("Cleanup of expired unassigned team members completed. Deleted {} members", expiredMembers.size());
    }
}
