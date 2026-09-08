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
import com.hokyozu.kyofuse.users.entity.AccountSuccessionRecord;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.SuccessionEntityType;
import com.hokyozu.kyofuse.users.repository.AccountSuccessionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountSuccessionService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final AccountSuccessionRecordRepository accountSuccessionRecordRepository;

    @Transactional
    public void handleOwnershipTransferAndDemotion(User user) {
        accountSuccessionRecordRepository.deleteAllByUser(user);
        transferTeamOwnershipsAndDemote(user);
        transferCommunityOwnershipsAndDemote(user);
    }

    @Transactional
    public void handleAccountReactivation(User user) {
        List<AccountSuccessionRecord> records = accountSuccessionRecordRepository.findAllByUser(user);

        if (!records.isEmpty()) {
            log.info("[AccountReactivation] Restaurando {} registros de liderança/cargos para o usuário {}",
                    records.size(), user.getUsername());

            for (AccountSuccessionRecord record : records) {
                if (record.getEntityType() == SuccessionEntityType.TEAM) {
                    restoreTeamSuccession(user, record);
                } else if (record.getEntityType() == SuccessionEntityType.COMMUNITY) {
                    restoreCommunitySuccession(user, record);
                }
            }

            accountSuccessionRecordRepository.deleteAllByUser(user);
        }

        reactivateFallbackTeams(user);
        reactivateFallbackCommunities(user);
    }

    private void transferTeamOwnershipsAndDemote(User user) {
        List<Team> ownedTeams = teamRepository.findAllByOwner(user);

        for (Team team : ownedTeams) {
            List<TeamMember> allMembers = teamMemberRepository.findByTeam(team);

            Optional<TeamMember> userMemberOpt = allMembers.stream()
                    .filter(m -> m.getUser().getId().equals(user.getId()))
                    .findFirst();

            Optional<TeamMember> successorOpt = allMembers.stream()
                    .filter(m -> !m.getUser().getId().equals(user.getId()) && m.getStatus() == TeamMemberStatus.ACTIVE)
                    .min(Comparator.comparingInt(this::getTeamMemberTier)
                            .thenComparing(TeamMember::getJoinedAt));

            String prevUserMemberType = userMemberOpt.map(m -> m.getMemberType() != null ? m.getMemberType().name() : null)
                    .orElse(TeamMemberType.MANAGER.name());
            String prevUserRoleInTeam = userMemberOpt.map(TeamMember::getRoleInTeam).map(Enum::name).orElse(null);

            String prevSuccessorMemberType = null;

            if (successorOpt.isPresent()) {
                TeamMember successor = successorOpt.get();
                prevSuccessorMemberType = successor.getMemberType() != null ? successor.getMemberType().name() : null;

                log.info("[Succession] Transferindo liderança do time '{}' de {} para {}",
                        team.getName(), user.getUsername(), successor.getUser().getUsername());

                team.setOwner(successor.getUser());
                team.setUpdatedAt(Instant.now());
                teamRepository.save(team);

                successor.setMemberType(TeamMemberType.MANAGER);
                successor.setUpdatedAt(Instant.now());
                teamMemberRepository.save(successor);
            } else {
                log.info("[Succession] Time '{}' sem outros membros ativos. Marcando como INACTIVE.", team.getName());
                team.setStatus(TeamStatus.INACTIVE);
                team.setUpdatedAt(Instant.now());
                teamRepository.save(team);
            }

            userMemberOpt.ifPresent(userMember -> {
                userMember.setMemberType(TeamMemberType.UNASSIGNED);
                userMember.setRoleInTeam(null);
                userMember.setUpdatedAt(Instant.now());
                teamMemberRepository.save(userMember);
            });

            AccountSuccessionRecord record = AccountSuccessionRecord.builder()
                    .user(user)
                    .entityType(SuccessionEntityType.TEAM)
                    .entityId(team.getId())
                    .successor(successorOpt.map(TeamMember::getUser).orElse(null))
                    .previousMemberType(prevUserMemberType)
                    .previousRoleInTeam(prevUserRoleInTeam)
                    .previousSuccessorMemberType(prevSuccessorMemberType)
                    .wasOwner(true)
                    .previousStatus(team.getStatus() != null ? team.getStatus().name() : TeamStatus.ACTIVE.name())
                    .createdAt(Instant.now())
                    .build();
            accountSuccessionRecordRepository.save(record);
        }

        List<TeamMember> userMemberships = teamMemberRepository.findByUser(user);
        for (TeamMember member : userMemberships) {
            if (!member.getTeam().getOwner().getId().equals(user.getId()) &&
                    (member.getMemberType() != TeamMemberType.UNASSIGNED || member.getRoleInTeam() != null)) {

                AccountSuccessionRecord record = AccountSuccessionRecord.builder()
                        .user(user)
                        .entityType(SuccessionEntityType.TEAM)
                        .entityId(member.getTeam().getId())
                        .previousMemberType(member.getMemberType() != null ? member.getMemberType().name() : null)
                        .previousRoleInTeam(member.getRoleInTeam() != null ? member.getRoleInTeam().name() : null)
                        .wasOwner(false)
                        .createdAt(Instant.now())
                        .build();
                accountSuccessionRecordRepository.save(record);

                member.setMemberType(TeamMemberType.UNASSIGNED);
                member.setRoleInTeam(null);
                member.setUpdatedAt(Instant.now());
                teamMemberRepository.save(member);
            }
        }
    }

    private void transferCommunityOwnershipsAndDemote(User user) {
        List<Community> ownedCommunities = communityRepository.findAllByOwner(user);

        for (Community community : ownedCommunities) {
            List<CommunityMember> activeMembers = communityMemberRepository
                    .findByCommunityAndStatus(community, CommunityMemberStatus.ACTIVE);

            Optional<CommunityMember> userMemberOpt = activeMembers.stream()
                    .filter(m -> m.getUser().getId().equals(user.getId()))
                    .findFirst();

            Optional<CommunityMember> successorOpt = activeMembers.stream()
                    .filter(m -> !m.getUser().getId().equals(user.getId()))
                    .min(Comparator.comparingInt(this::getCommunityMemberTier)
                            .thenComparing(CommunityMember::getJoinedAt));

            String prevUserRole = userMemberOpt.map(m -> m.getRole() != null ? m.getRole().name() : null)
                    .orElse(CommunityMemberRole.ADMIN.name());
            String prevSuccessorRole = null;

            if (successorOpt.isPresent()) {
                CommunityMember successor = successorOpt.get();
                prevSuccessorRole = successor.getRole() != null ? successor.getRole().name() : null;

                log.info("[Succession] Transferindo liderança da comunidade '{}' de {} para {}",
                        community.getName(), user.getUsername(), successor.getUser().getUsername());

                community.setOwner(successor.getUser());
                community.setUpdatedAt(Instant.now());
                communityRepository.save(community);

                successor.setRole(CommunityMemberRole.ADMIN);
                successor.setUpdatedAt(Instant.now());
                communityMemberRepository.save(successor);
            } else {
                log.info("[Succession] Comunidade '{}' sem outros membros ativos. Marcando como ARCHIVED.", community.getName());
                community.setStatus(CommunityStatus.ARCHIVED);
                community.setUpdatedAt(Instant.now());
                communityRepository.save(community);
            }

            userMemberOpt.ifPresent(userMember -> {
                userMember.setRole(CommunityMemberRole.MEMBER);
                userMember.setUpdatedAt(Instant.now());
                communityMemberRepository.save(userMember);
            });

            AccountSuccessionRecord record = AccountSuccessionRecord.builder()
                    .user(user)
                    .entityType(SuccessionEntityType.COMMUNITY)
                    .entityId(community.getId())
                    .successor(successorOpt.map(CommunityMember::getUser).orElse(null))
                    .previousRole(prevUserRole)
                    .previousSuccessorRole(prevSuccessorRole)
                    .wasOwner(true)
                    .previousStatus(community.getStatus() != null ? community.getStatus().name() : CommunityStatus.ACTIVE.name())
                    .createdAt(Instant.now())
                    .build();
            accountSuccessionRecordRepository.save(record);
        }
    }

    private void restoreTeamSuccession(User user, AccountSuccessionRecord record) {
        teamRepository.findById(record.getEntityId()).ifPresent(team -> {
            if (record.isWasOwner()) {
                log.info("[AccountReactivation] Restaurando dono do time '{}' de volta para {}",
                        team.getName(), user.getUsername());

                team.setOwner(user);
                if (team.getStatus() == TeamStatus.INACTIVE) {
                    team.setStatus(TeamStatus.ACTIVE);
                }
                team.setUpdatedAt(Instant.now());
                teamRepository.save(team);

                if (record.getSuccessor() != null) {
                    TeamMember successorMember = teamMemberRepository.findByTeamAndUser(team, record.getSuccessor());
                    if (successorMember != null) {
                        TeamMemberType revertType = record.getPreviousSuccessorMemberType() != null
                                ? TeamMemberType.valueOf(record.getPreviousSuccessorMemberType())
                                : TeamMemberType.PLAYER;
                        successorMember.setMemberType(revertType);
                        successorMember.setUpdatedAt(Instant.now());
                        teamMemberRepository.save(successorMember);
                    }
                }

                communityRepository.findByTeamId(team.getId()).ifPresent(comm -> {
                    if (comm.getStatus() == CommunityStatus.ARCHIVED) {
                        comm.setStatus(CommunityStatus.ACTIVE);
                        comm.setUpdatedAt(Instant.now());
                        communityRepository.save(comm);
                    }
                });
            }

            TeamMember userMember = teamMemberRepository.findByTeamAndUser(team, user);
            if (userMember != null) {
                boolean isOwner = record.isWasOwner() || (team.getOwner() != null && team.getOwner().getId().equals(user.getId()));
                TeamMemberType targetType;
                if (isOwner) {
                    targetType = TeamMemberType.MANAGER;
                } else if (record.getPreviousMemberType() != null && !TeamMemberType.UNASSIGNED.name().equals(record.getPreviousMemberType())) {
                    targetType = TeamMemberType.valueOf(record.getPreviousMemberType());
                } else {
                    targetType = TeamMemberType.PLAYER;
                }
                userMember.setMemberType(targetType);
                userMember.setRoleInTeam(record.getPreviousRoleInTeam() != null
                        ? com.hokyozu.kyofuse.profiles.enums.PlayerRole.valueOf(record.getPreviousRoleInTeam())
                        : null);
                userMember.setStatus(TeamMemberStatus.ACTIVE);
                userMember.setUpdatedAt(Instant.now());
                teamMemberRepository.save(userMember);
            }
        });
    }

    private void restoreCommunitySuccession(User user, AccountSuccessionRecord record) {
        communityRepository.findById(record.getEntityId()).ifPresent(community -> {
            if (record.isWasOwner()) {
                log.info("[AccountReactivation] Restaurando dono da comunidade '{}' de volta para {}",
                        community.getName(), user.getUsername());

                community.setOwner(user);
                if (community.getStatus() == CommunityStatus.ARCHIVED) {
                    community.setStatus(CommunityStatus.ACTIVE);
                }
                community.setUpdatedAt(Instant.now());
                communityRepository.save(community);

                if (record.getSuccessor() != null) {
                    communityMemberRepository.findByCommunityAndUser(community, record.getSuccessor()).ifPresent(successorMember -> {
                        CommunityMemberRole revertRole = record.getPreviousSuccessorRole() != null
                                ? CommunityMemberRole.valueOf(record.getPreviousSuccessorRole())
                                : CommunityMemberRole.MEMBER;
                        successorMember.setRole(revertRole);
                        successorMember.setUpdatedAt(Instant.now());
                        communityMemberRepository.save(successorMember);
                    });
                }
            }

            communityMemberRepository.findByCommunityAndUser(community, user).ifPresent(userMember -> {
                CommunityMemberRole targetRole = record.getPreviousRole() != null
                        ? CommunityMemberRole.valueOf(record.getPreviousRole())
                        : (record.isWasOwner() ? CommunityMemberRole.ADMIN : CommunityMemberRole.MEMBER);
                userMember.setRole(targetRole);
                userMember.setStatus(CommunityMemberStatus.ACTIVE);
                userMember.setUpdatedAt(Instant.now());
                communityMemberRepository.save(userMember);
            });
        });
    }

    private void reactivateFallbackTeams(User user) {
        List<Team> ownedTeams = teamRepository.findAllByOwner(user);
        for (Team team : ownedTeams) {
            if (team.getStatus() == TeamStatus.INACTIVE) {
                team.setStatus(TeamStatus.ACTIVE);
                team.setUpdatedAt(Instant.now());
                teamRepository.save(team);
            }

            List<TeamMember> members = teamMemberRepository.findByTeam(team);
            for (TeamMember member : members) {
                if (member.getUser().getId().equals(user.getId())) {
                    member.setMemberType(TeamMemberType.MANAGER);
                    member.setStatus(TeamMemberStatus.ACTIVE);
                    member.setUpdatedAt(Instant.now());
                    teamMemberRepository.save(member);
                }
            }

            communityRepository.findByTeamId(team.getId()).ifPresent(comm -> {
                if (comm.getStatus() == CommunityStatus.ARCHIVED) {
                    comm.setStatus(CommunityStatus.ACTIVE);
                    comm.setUpdatedAt(Instant.now());
                    communityRepository.save(comm);
                }
            });
        }
    }

    private void reactivateFallbackCommunities(User user) {
        List<Community> ownedCommunities = communityRepository.findAllByOwner(user);
        for (Community community : ownedCommunities) {
            if (community.getStatus() == CommunityStatus.ARCHIVED) {
                community.setStatus(CommunityStatus.ACTIVE);
                community.setUpdatedAt(Instant.now());
                communityRepository.save(community);

                communityMemberRepository.findByCommunityAndUser(community, user).ifPresent(member -> {
                    member.setRole(CommunityMemberRole.ADMIN);
                    member.setStatus(CommunityMemberStatus.ACTIVE);
                    member.setUpdatedAt(Instant.now());
                    communityMemberRepository.save(member);
                });
            }
        }
    }

    private int getTeamMemberTier(TeamMember member) {
        if (member.getMemberType() == null) return 5;
        return switch (member.getMemberType()) {
            case MANAGER -> 1;
            case COACH -> 2;
            case ANALYST -> 3;
            case PLAYER, SUBSTITUTE -> 4;
            case UNASSIGNED -> 5;
        };
    }

    private int getCommunityMemberTier(CommunityMember member) {
        if (member.getRole() == null) return 3;
        return switch (member.getRole()) {
            case ADMIN -> 1;
            case MODERATOR -> 2;
            case MEMBER -> 3;
        };
    }
}
