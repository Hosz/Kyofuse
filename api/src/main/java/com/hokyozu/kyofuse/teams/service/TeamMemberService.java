package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.communities.service.CommunityMemberService;
import com.hokyozu.kyofuse.invites.entity.TeamInvite;
import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.invites.repository.TeamInviteRepository;
import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.teams.dto.request.TeamMemberEditRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.finder.TeamFinder;
import com.hokyozu.kyofuse.teams.mapper.TeamMemberMapper;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final UserFinder userFinder;
    private final TeamFinder teamFinder;
    private final GamerProfileFinder gamerProfileFinder;
    private final TeamRequiredRoleFulfillment teamRequiredRoleFulfillment;
    private final UserChecker userChecker;

    private final TeamMemberRepository teamMemberRepository;
    private final TeamChecker teamChecker;
    private final TeamInviteRepository teamInviteRepository;
    private final NotificationService notificationService;
    private final CommunityRepository communityRepository;
    private final CommunityMemberService communityMemberService;

    @Transactional
    public TeamMemberResponse addMember(String teamIdentifier, UUID userId, UUID userInvitedId) {
        Team team = teamFinder.findTeamByIdentifier(teamIdentifier);
        return addMemberInternal(team, userId, userInvitedId);
    }

    @Transactional
    public TeamMemberResponse addMember(UUID teamId, UUID userId, UUID userInvitedId) {
        Team team = teamFinder.findTeamById(teamId);
        return addMemberInternal(team, userId, userInvitedId);
    }

    private TeamMemberResponse addMemberInternal(Team team, UUID userId, UUID userInvitedId) {
        User user = userFinder.findProfileByUserId(userId);
        User userInvited = userFinder.findProfileByUserId(userInvitedId);
        boolean hasInvite = teamInviteRepository.existsByTeamAndReceiverAndStatus(team, userInvited, TeamInviteStatus.PENDING);

        teamChecker.checkInactive(team);
        teamChecker.checkUserIsOwner(team, user);

        userChecker.checkActive(user);
        userChecker.checkActive(userInvited);

        if (teamMemberRepository.existsByTeamAndUser(team, userInvited)) {
            throw new BadRequestException("User is already a member of the team.");
        }

        TeamMember savedTeamMember;
        if (hasInvite) {
            try {
                TeamMember teamMember = TeamMemberMapper.toEntity(userInvited, team);
                savedTeamMember = teamMemberRepository.save(teamMember);
            } catch (Exception e) {
                throw new BadRequestException("Failed to update the invite status. Error: " + e.getMessage());
            }
        } else {
            TeamMember teamMember = TeamMemberMapper.toEntity(userInvited, team);
            savedTeamMember = teamMemberRepository.save(teamMember);

            notificationService.createNotification(
                    CreateNotificationRequest.builder()
                            .recipient(userInvited)
                            .actor(user)
                            .type(NotificationType.TEAM_MEMBER_ADDED)
                            .title("Novo membro do time.")
                            .message(user.getUsername() + " adicionou você ao time.")
                            .targetType(NotificationTargetType.TEAM)
                            .targetId(teamMember.getId())
                            .metadata(Map.of(
                                    "TeamName", team.getName()
                            ))
                            .build()
            );
        }

        if (communityRepository != null && communityMemberService != null) {
            try {
                communityRepository.findByTeamId(team.getId()).ifPresent(comm -> {
                    try {
                        communityMemberService.addMember(userInvited, comm);
                        log.info("[TeamMember] Membro {} adicionado à comunidade vinculada '{}'", userInvited.getUsername(), comm.getName());
                    } catch (Exception ex) {
                        log.warn("[TeamMember] Não foi possível adicionar membro na comunidade do time: {}", ex.getMessage());
                    }
                });
            } catch (Exception ex) {
                log.warn("[TeamMember] Erro ao sincronizar membro com a comunidade do time: {}", ex.getMessage());
            }
        }

        return TeamMemberMapper.toResponse(savedTeamMember);
    }

    @Transactional
    public TeamMemberResponse editMember(String teamIdentifier, UUID userEditedId, UUID userId, TeamMemberEditRequest request) {
        Team team = teamFinder.findTeamByIdentifier(teamIdentifier);
        return editMemberInternal(team, userEditedId, userId, request);
    }

    @Transactional
    public TeamMemberResponse editMember(UUID teamId, UUID userEditedId, UUID userId, TeamMemberEditRequest request) {
        Team team = teamFinder.findTeamById(teamId);
        return editMemberInternal(team, userEditedId, userId, request);
    }

    private TeamMemberResponse editMemberInternal(Team team, UUID userEditedId, UUID userId, TeamMemberEditRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        User userEdited = userFinder.findProfileByUserId(userEditedId);

        teamChecker.checkInactive(team);
        teamChecker.checkUserIsOwner(team, user);

        userChecker.checkActive(user);
        userChecker.checkActive(userEdited);

        if (!teamMemberRepository.existsByTeamAndUser(team, userEdited)) {
            throw new BadRequestException("User is not a member of the team.");
        }

        if (request != null && request.roleInTeam() == null && request.memberType() == null) {
            throw new BadRequestException("At least one field must be provided for update.");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, userEdited);

        TeamMemberMapper.toUpdate(teamMember, request);
        if (teamMember.getMemberType() != TeamMemberType.UNASSIGNED) {
            teamMember.setAssignmentDueAt(null);
        }

        teamMemberRepository.save(teamMember);

        // A função passou a ter dono: se o time anunciava essa vaga, ela sai do anúncio.
        teamRequiredRoleFulfillment.fulfill(team, request.roleInTeam());

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(userEdited)
                        .actor(user)
                        .type(NotificationType.TEAM_MEMBER_EDITED)
                        .title("Membro do time atualizado.")
                        .message(user.getUsername() + " atualizou seu status no time.")
                        .targetType(NotificationTargetType.TEAM)
                        .targetId(teamMember.getId())
                        .metadata(Map.of(
                                "TeamName", team.getName()
                        ))
                        .build()
        );
        return TeamMemberMapper.toResponse(teamMember);
    }

    @Transactional(readOnly = true)
    public Page<TeamMemberResponse> listMembers(UUID teamId, UUID userId, Pageable pageable) {
        Team team = teamFinder.findTeamById(teamId);
        return listMembersInternal(team, userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TeamMemberResponse> listMembers(String teamIdentifier, UUID userId, Pageable pageable) {
        Team team;
        try {
            team = teamFinder.findTeamById(UUID.fromString(teamIdentifier));
        } catch (IllegalArgumentException e) {
            team = teamFinder.findTeamBySlug(teamIdentifier);
        }
        return listMembersInternal(team, userId, pageable);
    }

    private Page<TeamMemberResponse> listMembersInternal(Team team, UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);

        userChecker.checkActive(user);
        teamChecker.checkInactive(team);

        Page<TeamMember> teamMembers = teamMemberRepository.findByTeam(team, pageable);

        // Apelido e foto vêm do perfil, em lote: um lookup por membro faria o número de
        // consultas crescer junto com o tamanho do time.
        List<UUID> memberIds = teamMembers.getContent().stream()
                .map(member -> member.getUser().getId())
                .distinct()
                .toList();
        Map<UUID, GamerProfile> profiles = memberIds.isEmpty()
                ? Map.of()
                : gamerProfileFinder.findAllByUserIds(memberIds).stream()
                        .collect(Collectors.toMap(profile -> profile.getUser().getId(), java.util.function.Function.identity(), (a, b) -> a));

        return teamMembers.map(member -> TeamMemberMapper.toResponse(member, profiles.get(member.getUser().getId())));
    }

    @Transactional
    public void removeMember(String teamIdentifier, UUID userId, UUID userRemovedId) {
        Team team = teamFinder.findTeamByIdentifier(teamIdentifier);
        removeMemberInternal(team, userId, userRemovedId);
    }

    @Transactional
    public void removeMember(UUID teamId, UUID userId, UUID userRemovedId) {
        Team team = teamFinder.findTeamById(teamId);
        removeMemberInternal(team, userId, userRemovedId);
    }

    private void removeMemberInternal(Team team, UUID userId, UUID userRemovedId) {
        User user = userFinder.findProfileByUserId(userId);
        User userRemoved = userFinder.findProfileByUserId(userRemovedId);

        teamChecker.checkInactive(team);
        teamChecker.checkUserIsOwner(team, user);

        userChecker.checkActive(user);
        userChecker.checkActive(userRemoved);

        if (team.getOwner().getId().equals(userRemoved.getId())) {
            throw new BadRequestException("Team owner cannot be removed from the team.");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, userRemoved);
        if (teamMember == null) {
            throw new BadRequestException("Member does not exist in this team.");
        }

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(userRemoved)
                        .actor(user)
                        .type(NotificationType.TEAM_MEMBER_REMOVED)
                        .title("Membro removido do time.")
                        .message(user.getUsername() + " removeu você do time.")
                        .targetType(NotificationTargetType.TEAM)
                        .targetId(team.getId())
                        .metadata(Map.of(
                                "TeamName", team.getName()
                        ))
                        .build()
        );

        teamMemberRepository.delete(teamMember);
    }

    @Transactional
    public void leaveTeam(String teamIdentifier, UUID userId) {
        Team team = teamFinder.findTeamByIdentifier(teamIdentifier);
        leaveTeamInternal(team, userId);
    }

    @Transactional
    public void leaveTeam(UUID teamId, UUID userId) {
        Team team = teamFinder.findTeamById(teamId);
        leaveTeamInternal(team, userId);
    }

    private void leaveTeamInternal(Team team, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);

        userChecker.checkActive(user);

        if (team.getOwner().getId().equals(user.getId())) {
            throw new BadRequestException("Team owner cannot leave the team.");
        }

        if (!teamMemberRepository.existsByTeamAndUser(team, user)) {
            throw new BadRequestException("User is not a member of the team.");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, user);
        if (teamMember == null) {
            throw new BadRequestException("Member does not exist in this team.");
        }

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(team.getOwner())
                        .actor(user)
                        .type(NotificationType.TEAM_MEMBER_LEFT)
                        .title("Membro saiu do time.")
                        .message(user.getUsername() + " saiu do time.")
                        .targetType(NotificationTargetType.TEAM)
                        .targetId(team.getId())
                        .metadata(Map.of(
                                "TeamName", team.getName()
                        ))
                        .build()
        );

        teamMemberRepository.delete(teamMember);
    }

    @Transactional(readOnly = true)
    public TeamMemberResponse detailMember(String teamIdentifier, UUID userId, UUID teamMemberId) {
        Team team = teamFinder.findTeamByIdentifier(teamIdentifier);
        return detailMemberInternal(team, userId, teamMemberId);
    }

    @Transactional(readOnly = true)
    public TeamMemberResponse detailMember(UUID teamId, UUID userId, UUID teamMemberId) {
        Team team = teamFinder.findTeamById(teamId);
        return detailMemberInternal(team, userId, teamMemberId);
    }

    private TeamMemberResponse detailMemberInternal(Team team, UUID userId, UUID teamMemberId) {
        User user = userFinder.findProfileByUserId(userId);
        User teamMemberUser = userFinder.findProfileByUserId(teamMemberId);

        userChecker.checkActive(user);
        teamChecker.checkInactive(team);

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, teamMemberUser);

        if (teamMember == null) {
            throw new BadRequestException("Team member not found.");
        }

        if (!teamMember.getTeam().getId().equals(team.getId())) {
            throw new BadRequestException("Team member does not belong to the specified team.");
        }

        return TeamMemberMapper.toResponse(teamMember);
    }
}
