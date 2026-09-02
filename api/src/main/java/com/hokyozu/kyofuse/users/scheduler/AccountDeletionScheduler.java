package com.hokyozu.kyofuse.users.scheduler;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AccountSwitchSessionRepository;
import com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenRepository;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
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
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountDeletionScheduler {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final AccountSwitchSessionRepository accountSwitchSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GamerProfileRepository gamerProfileRepository;
    private final EmailCipherService emailCipherService;
    private final com.hokyozu.kyofuse.users.repository.AccountSuccessionRecordRepository accountSuccessionRecordRepository;

    @Scheduled(cron = "0 0 * * * *") // Hourly
    @Transactional
    public void executePermanentSoftDeletion() {
        Instant now = Instant.now();
        List<User> usersToDelete = userRepository.findByStatusAndDeletionScheduledAtBefore(UserStatus.INACTIVE, now);

        if (usersToDelete.isEmpty()) {
            return;
        }

        log.info("[AccountDeletion] Executando exclusão definitiva (soft-delete/anonimização) para {} contas.", usersToDelete.size());

        for (User user : usersToDelete) {
            UUID userId = user.getId();
            String idPrefix = userId.toString().substring(0, 8);

            // Remove from teams
            teamMemberRepository.findByUser(user).forEach(tm -> {
                tm.setStatus(TeamMemberStatus.REMOVED);
                tm.setLeftAt(now);
                tm.setUpdatedAt(now);
                teamMemberRepository.save(tm);
            });

            // Remove from communities
            communityMemberRepository.findByUserAndStatus(user, CommunityMemberStatus.ACTIVE, org.springframework.data.domain.Pageable.unpaged())
                    .forEach(cm -> {
                        cm.setStatus(CommunityMemberStatus.LEFT);
                        cm.setLeftAt(now);
                        cm.setUpdatedAt(now);
                        communityMemberRepository.save(cm);
                    });

            // Clear sessions & tokens
            accountSwitchSessionRepository.deleteAllByUserId(userId);
            refreshTokenRepository.deleteAllByUser(user);
            accountSuccessionRecordRepository.deleteAllByUser(user);

            // Anonymize Gamer Profile
            gamerProfileRepository.findByUserId(userId).ifPresent(profile -> {
                profile.setNickname("Usuário Excluído");
                profile.setAvatarUrl(null);
                profile.setBannerUrl(null);
                profile.setBio(null);
                profile.setUpdatedAt(now);
                gamerProfileRepository.save(profile);
            });

            // Anonymize User PII
            String anonEmail = "deleted_" + userId + "@deleted.kyofuse.local";
            user.setUsername("deleted_" + idPrefix);
            user.setFirstName("Usuário");
            user.setLastName("Excluído");
            user.setEmail(anonEmail);
            user.setEmailIndex(emailCipherService.blindIndex(anonEmail));
            user.setPasswordHash("DELETED_" + UUID.randomUUID());
            user.setGoogleId(null);
            user.setSteamId(null);
            user.setTotpSecret(null);
            user.setTotpEnabled(false);
            user.setHasCustomPassword(false);
            user.setDeletionScheduledAt(null);
            user.setUpdatedAt(now);
            userRepository.save(user);

            log.info("[AccountDeletion] Conta {} anonimizada com sucesso preservando histórico para auditoria.", userId);
        }
    }
}
