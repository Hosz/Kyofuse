package com.hokyozu.kyofuse.invites.service;

import com.hokyozu.kyofuse.invites.dto.request.TeamInviteCancelRequest;
import com.hokyozu.kyofuse.invites.dto.request.TeamInviteRequest;
import com.hokyozu.kyofuse.invites.dto.response.TeamInviteResponse;
import com.hokyozu.kyofuse.invites.entity.TeamInvite;
import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.invites.mapper.TeamInviteMapper;
import com.hokyozu.kyofuse.invites.repository.TeamInviteRepository;
import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.TeamInvitePermission;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.finder.TeamFinder;
import com.hokyozu.kyofuse.teams.mapper.TeamMemberMapper;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.teams.service.TeamChecker;
import com.hokyozu.kyofuse.teams.service.TeamRequiredRoleFulfillment;
import com.hokyozu.kyofuse.teams.service.TeamMemberService;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamInviteService {

    private final TeamInviteRepository teamInviteRepository;
    private final TeamMemberRepository teamMemberRepository;

    private final UserFinder userFinder;
    private final TeamFinder teamFinder;

    private final UserChecker userChecker;
    private final TeamChecker teamChecker;

    private final TeamMemberService teamMemberService;
    private final TeamRequiredRoleFulfillment teamRequiredRoleFulfillment;
    private final NotificationService notificationService;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final UserFriendshipRepository userFriendshipRepository;
    private final BlockValidator blockValidator;

    @Transactional
    public TeamInviteResponse inviteUser(UUID userId, UUID teamId, String receiverUsername, TeamInviteRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        User receiver = userFinder.findProfileByUsername(receiverUsername);
        Team team = teamFinder.findTeamById(teamId);

        userChecker.checkActive(user);
        userChecker.checkActive(receiver);
        teamChecker.checkInactive(team);
        teamChecker.checkUserIsOwner(team, user);

        blockValidator.validate(user, receiver);

        if (receiver.getId().equals(team.getOwner().getId())) {
            throw new BadRequestException("You cannot invite the team owner.");
        }

        if (userId.equals(receiver.getId())) {
            throw new BadRequestException("You cannot invite yourself to a team.");
        }

        UserPrivacySettings receiverSettings = userPrivacySettingsRepository.findByUser(receiver);
        TeamInvitePermission invitePermission = receiverSettings != null ? receiverSettings.getTeamInvitePermission() : TeamInvitePermission.EVERYONE;

        if (invitePermission == TeamInvitePermission.NOBODY) {
            throw new ForbiddenException("This user does not accept team invites.");
        }

        if (invitePermission == TeamInvitePermission.FRIENDS) {
            boolean isFriend = userFriendshipRepository.existsByUserOneAndUserTwo(user, receiver) ||
                    userFriendshipRepository.existsByUserOneAndUserTwo(receiver, user);
            if (!isFriend) {
                throw new ForbiddenException("This user only accepts team invites from friends.");
            }
        }

        if (teamInviteRepository.existsByTeamAndReceiverAndStatus(team, receiver, TeamInviteStatus.PENDING)) {
            throw new BadRequestException("An invite has already been sent to this user for the team.");
        }

        if (teamMemberRepository.existsByTeamAndUser(team, receiver)) {
            throw new BadRequestException("The user is already a member of the team.");
        }

        if (userId == receiver.getId()) {
            throw new BadRequestException("You cannot invite yourself to a team.");
        }

        TeamInvite teamInvite = TeamInviteMapper.toEntity(team, user, receiver, request);
        teamInviteRepository.save(teamInvite);

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(receiver)
                        .actor(user)
                        .type(NotificationType.TEAM_INVITE_RECEIVED)
                        .title("Nova convite.")
                        .message(user.getUsername() + " convidou você para se juntar a um time.")
                        .targetType(NotificationTargetType.TEAM_INVITE)
                        .targetId(teamInvite.getId())
                        .metadata(Map.of(
                                "TeamName", team.getName()
                        ))
                        .build()
        );

        return TeamInviteMapper.toResponse(teamInvite);
    }

    @Transactional(readOnly = true)
    public Page<TeamInviteResponse> listInvites(UUID userId, UUID teamId, Pageable pageable, TeamInviteStatus status) {
        User user = userFinder.findProfileByUserId(userId);
        Team team = teamFinder.findTeamById(teamId);

        userChecker.checkActive(user);
        teamChecker.checkInactive(team);
        teamChecker.checkUserIsOwner(team, user);

        if (status == null) {
            Page<TeamInvite> invites = teamInviteRepository.findAllByTeam(team, pageable);
            return invites.map(TeamInviteMapper::toResponse);
        }
        Page<TeamInvite> invites = teamInviteRepository.findAllByTeamAndStatus(team, pageable, status);
        return invites.map(TeamInviteMapper::toResponse);
    }

    @com.hokyozu.kyofuse.infrastructure.redis.DistributedLock(key = "'team:invite:' + #inviteId", leaseTimeSeconds = 5)
    @Transactional
    public void acceptInvite(UUID userId, UUID inviteId) {
        User user = userFinder.findProfileByUserId(userId);
        TeamInvite invite = teamInviteRepository.findById(inviteId)
                .orElseThrow(() -> new BadRequestException("Invite not found for ID: " + inviteId));
        Team team = teamFinder.findTeamById(invite.getTeam().getId());
        boolean hasInvite = teamInviteRepository.existsByTeamAndReceiverAndStatus(team, user, TeamInviteStatus.PENDING);

        userChecker.checkActive(user);
        teamChecker.checkInactive(invite.getTeam());

        if (!user.getId().equals(invite.getReceiver().getId())) {
            throw new BadRequestException("You are not authorized to accept this invite.");
        }

        switch (invite.getStatus()) {
            case ACCEPTED:
                throw new BadRequestException("Esse convite ja foi aceito.");
            case DECLINED:
                throw new BadRequestException("Esse convite ja foi rejeitado.");
            case CANCELED:
                throw new BadRequestException("Esse convite foi cancelado.");
            default:
                break;
        }

        if (hasInvite && teamMemberRepository.existsByTeamAndUser(team, user)) {
            invite.setStatus(TeamInviteStatus.ACCEPTED);
            invite.setRespondedAt(Instant.now());
            teamInviteRepository.save(invite);
            notificationService.markInviteAsAccepted(user.getId(), invite.getId());
            return;
        }

        teamMemberService.addMember(team.getId(), invite.getSender().getId(), invite.getReceiver().getId());

        invite.setStatus(TeamInviteStatus.ACCEPTED);
        invite.setRespondedAt(Instant.now());
        teamInviteRepository.save(invite);

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, invite.getReceiver());
        teamMember.setRoleInTeam(invite.getProposedRoleInTeam());
        teamMember.setMemberType(invite.getProposedMemberType());
        if (teamMember.getMemberType() != TeamMemberType.UNASSIGNED) {
            teamMember.setAssignmentDueAt(null);
        }

        teamMemberRepository.save(teamMember);

        // Mesma regra da edição: vaga anunciada e agora preenchida sai do anúncio.
        teamRequiredRoleFulfillment.fulfill(team, invite.getProposedRoleInTeam());

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(invite.getSender())
                        .actor(user)
                        .type(NotificationType.TEAM_INVITE_ACCEPTED)
                        .title("Convite aceito.")
                        .message(user.getUsername() + " Aceitou o convite do time.")
                        .targetType(NotificationTargetType.TEAM_INVITE)
                        .targetId(invite.getId())
                        .metadata(Map.of(
                                "TeamName", team.getName()
                        ))
                        .build()
        );

        notificationService.markInviteAsAccepted(user.getId(), invite.getId());
    }

    @Transactional
    public void declineInvite(UUID userId, UUID inviteId) {
        User user = userFinder.findProfileByUserId(userId);
        TeamInvite invite = teamInviteRepository.findById(inviteId)
                .orElseThrow(() -> new BadRequestException("Invite not found for ID: " + inviteId));

        userChecker.checkActive(user);
        teamChecker.checkInactive(invite.getTeam());

        switch (invite.getStatus()) {
            case DECLINED:
                throw new BadRequestException("Esse convite ja foi rejeitado.");
            case CANCELED:
                throw new BadRequestException("Esse convite foi cancelado.");
            case ACCEPTED:
                throw new BadRequestException("Esse convite ja foi aceito.");
            default:
                break;
        }

        if (!user.getId().equals(invite.getReceiver().getId())) {
            throw new BadRequestException("You are not authorized to decline this invite.");
        }

        invite.setStatus(TeamInviteStatus.DECLINED);
        invite.setRespondedAt(java.time.Instant.now());
        teamInviteRepository.save(invite);

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(invite.getSender())
                        .actor(user)
                        .type(NotificationType.TEAM_INVITE_DECLINED)
                        .title("Convite rejeitado.")
                        .message(user.getUsername() + " Rejeitou o convite do time.")
                        .targetType(NotificationTargetType.TEAM_INVITE)
                        .targetId(invite.getId())
                        .metadata(Map.of(
                                "TeamName", invite.getTeam().getName()
                        ))
                        .build()
        );

        notificationService.markInviteAsDeclined(user.getId(), invite.getId());
    }

    @Transactional
    public void cancelInvite(UUID userId, UUID inviteId, TeamInviteCancelRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        TeamInvite invite = teamInviteRepository.findById(inviteId)
                .orElseThrow(() -> new BadRequestException("Invite not found for ID: " + inviteId));
        Team team = teamFinder.findTeamById(invite.getTeam().getId());

        userChecker.checkActive(user);
        teamChecker.checkInactive(invite.getTeam());
        teamChecker.checkUserIsOwner(team, user);

        switch (invite.getStatus()) {
            case DECLINED:
                throw new BadRequestException("Esse convite ja foi rejeitado.");
            case CANCELED:
                throw new BadRequestException("Esse convite foi cancelado.");
            case ACCEPTED:
                throw new BadRequestException("Esse convite ja foi aceito.");
            default:
                break;
        }

        invite.setStatus(TeamInviteStatus.CANCELED);
        invite.setCancellationReason(request.cancellationReason());
        invite.setCanceledAt(Instant.now());
        invite.setCanceledBy(user);
        teamInviteRepository.save(invite);

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(invite.getReceiver())
                        .actor(user)
                        .type(NotificationType.TEAM_INVITE_CANCELED)
                        .title("Convite cancelado.")
                        .message(user.getUsername() + " Cancelou o convite do time.")
                        .targetType(NotificationTargetType.TEAM_INVITE)
                        .targetId(invite.getId())
                        .metadata(Map.of(
                                "TeamName", team.getName()
                        ))
                        .build()
        );

        notificationService.markInviteAsCanceled(invite.getReceiver().getId(), invite.getId());
    }
}
