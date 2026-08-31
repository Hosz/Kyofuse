package com.hokyozu.kyofuse.users.scheduler;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeactivatedAccountCleanupScheduler {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final com.hokyozu.kyofuse.users.repository.AccountSuccessionRecordRepository accountSuccessionRecordRepository;

    @Scheduled(cron = "0 0 3 * * *") // Daily at 3 AM
    @Transactional
    public void cleanupExpiredDeactivatedMemberships() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        List<User> expiredUsers = userRepository
                .findByStatusAndDeactivatedAtBeforeAndDeletionScheduledAtIsNull(UserStatus.INACTIVE, cutoff);

        if (expiredUsers.isEmpty()) {
            return;
        }

        log.info("[DeactivatedCleanup] Iniciando limpeza de vínculos para {} contas desativadas há mais de 30 dias.", expiredUsers.size());

        for (User user : expiredUsers) {
            // Remove from teams
            List<TeamMember> teamMembers = teamMemberRepository.findByUser(user);
            for (TeamMember member : teamMembers) {
                if (member.getStatus() == TeamMemberStatus.ACTIVE) {
                    member.setStatus(TeamMemberStatus.REMOVED);
                    member.setLeftAt(Instant.now());
                    member.setUpdatedAt(Instant.now());
                    teamMemberRepository.save(member);
                }
            }

            // Remove from communities
            List<CommunityMember> communityMembers = communityMemberRepository
                    .findByUserAndStatus(user, CommunityMemberStatus.ACTIVE, org.springframework.data.domain.Pageable.unpaged())
                    .getContent();
            for (CommunityMember member : communityMembers) {
                member.setStatus(CommunityMemberStatus.LEFT);
                member.setLeftAt(Instant.now());
                member.setUpdatedAt(Instant.now());
                communityMemberRepository.save(member);
            }

            // Clean up temporary succession records
            accountSuccessionRecordRepository.deleteAllByUser(user);
        }
    }
}
