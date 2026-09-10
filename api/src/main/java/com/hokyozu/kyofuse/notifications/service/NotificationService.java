package com.hokyozu.kyofuse.notifications.service;

import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.dto.response.NotificationResponse;
import com.hokyozu.kyofuse.notifications.entity.Notification;
import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.mapper.NotificationMapper;
import com.hokyozu.kyofuse.notifications.repository.NotificationRepository;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import com.hokyozu.kyofuse.invites.entity.TeamInvite;
import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.invites.repository.TeamInviteRepository;
import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final GamerProfileFinder gamerProfileFinder;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationCounterService notificationCounterService;
    private final TeamInviteRepository teamInviteRepository;
    private final UserFollowRepository userFollowRepository;

    @Transactional
    public Notification createNotification(CreateNotificationRequest request) {
        Notification notification = NotificationMapper.toEntity(request);
        Notification saved = notificationRepository.save(notification);

        notificationCounterService.increment(saved.getUser().getId());
        sendNotification(saved.getUser().getId(), notificationMapper.toResponse(saved));
        return saved;
    }

    public Page<NotificationResponse> listNotifications(
            UUID userId,
            Pageable pageable) {

        Page<Notification> notifications = notificationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<UUID> actorIds = notifications.getContent().stream()
                .map(Notification::getActor)
                .filter(Objects::nonNull)
                .map(User::getId)
                .distinct()
                .toList();

        Map<UUID, GamerProfile> actorProfiles = actorIds.isEmpty()
                ? Map.of()
                : gamerProfileFinder.findAllByUserIds(actorIds).stream()
                        .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        List<UUID> teamInviteIds = notifications.getContent().stream()
                .filter(n -> n.getTargetType() == NotificationTargetType.TEAM_INVITE && n.getTargetId() != null)
                .map(Notification::getTargetId)
                .distinct()
                .toList();

        Map<UUID, TeamInviteStatus> inviteStatusMap = teamInviteIds.isEmpty()
                ? Map.of()
                : teamInviteRepository.findAllById(teamInviteIds).stream()
                        .collect(Collectors.toMap(TeamInvite::getId, TeamInvite::getStatus, (a, b) -> a));

        return notifications.map(notification -> {
            GamerProfile actorProfile = notification.getActor() != null
                    ? actorProfiles.get(notification.getActor().getId())
                    : null;
            NotificationResponse response = notificationMapper.toResponse(notification, actorProfile);

            if (notification.getType() == NotificationType.TEAM_INVITE_RECEIVED && notification.getTargetId() != null) {
                TeamInviteStatus inviteStatus = inviteStatusMap.get(notification.getTargetId());
                if (inviteStatus == TeamInviteStatus.ACCEPTED) {
                    return response.toBuilder().type(NotificationType.TEAM_INVITE_ACCEPTED).title("Convite aceito.").build();
                } else if (inviteStatus == TeamInviteStatus.DECLINED) {
                    return response.toBuilder().type(NotificationType.TEAM_INVITE_DECLINED).title("Convite recusado.").build();
                } else if (inviteStatus == TeamInviteStatus.CANCELED) {
                    return response.toBuilder().type(NotificationType.TEAM_INVITE_CANCELED).title("Convite cancelado.").build();
                }
            } else if (notification.getType() == NotificationType.FOLLOW_REQUEST_RECEIVED && notification.getActor() != null) {
                boolean pending = userFollowRepository.existsByFollowerAndFollowedAndStatus(
                        notification.getActor(), notification.getUser(), FollowStatus.PENDING);
                if (!pending) {
                    boolean active = userFollowRepository.existsByFollowerAndFollowed(
                            notification.getActor(), notification.getUser());
                    if (active) {
                        return response.toBuilder().type(NotificationType.FOLLOW_REQUEST_ACCEPTED).title("Solicitação aceita.").build();
                    } else {
                        return response.toBuilder().type(NotificationType.FOLLOW_REQUEST_DECLINED).title("Solicitação recusada.").build();
                    }
                }
            }

            return response;
        });
    }

    @Transactional
    public void markInviteAsAccepted(UUID userId, UUID inviteId) {
        notificationRepository.findAllByUserIdAndTargetTypeAndTargetId(userId, NotificationTargetType.TEAM_INVITE, inviteId)
                .forEach(notification -> {
                    notification.setType(NotificationType.TEAM_INVITE_ACCEPTED);
                    notification.setTitle("Convite aceito.");
                    if (notification.getStatus() == NotificationStatus.UNREAD) {
                        notificationCounterService.decrement(userId);
                    }
                    notification.setStatus(NotificationStatus.READ);
                    notification.setReadAt(Instant.now());
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markInviteAsDeclined(UUID userId, UUID inviteId) {
        notificationRepository.findAllByUserIdAndTargetTypeAndTargetId(userId, NotificationTargetType.TEAM_INVITE, inviteId)
                .forEach(notification -> {
                    notification.setType(NotificationType.TEAM_INVITE_DECLINED);
                    notification.setTitle("Convite recusado.");
                    if (notification.getStatus() == NotificationStatus.UNREAD) {
                        notificationCounterService.decrement(userId);
                    }
                    notification.setStatus(NotificationStatus.READ);
                    notification.setReadAt(Instant.now());
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markInviteAsCanceled(UUID receiverId, UUID inviteId) {
        notificationRepository.findAllByUserIdAndTargetTypeAndTargetId(receiverId, NotificationTargetType.TEAM_INVITE, inviteId)
                .forEach(notification -> {
                    notification.setType(NotificationType.TEAM_INVITE_CANCELED);
                    notification.setTitle("Convite cancelado.");
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markFollowRequestAsAccepted(UUID userId, UUID senderId) {
        notificationRepository.findAllByUserIdAndTargetTypeAndTargetId(userId, NotificationTargetType.FOLLOW, senderId)
                .forEach(notification -> {
                    notification.setType(NotificationType.FOLLOW_REQUEST_ACCEPTED);
                    notification.setTitle("Solicitação aceita.");
                    if (notification.getStatus() == NotificationStatus.UNREAD) {
                        notificationCounterService.decrement(userId);
                    }
                    notification.setStatus(NotificationStatus.READ);
                    notification.setReadAt(Instant.now());
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markFollowRequestAsDeclined(UUID userId, UUID senderId) {
        notificationRepository.findAllByUserIdAndTargetTypeAndTargetId(userId, NotificationTargetType.FOLLOW, senderId)
                .forEach(notification -> {
                    notification.setType(NotificationType.FOLLOW_REQUEST_DECLINED);
                    notification.setTitle("Solicitação recusada.");
                    if (notification.getStatus() == NotificationStatus.UNREAD) {
                        notificationCounterService.decrement(userId);
                    }
                    notification.setStatus(NotificationStatus.READ);
                    notification.setReadAt(Instant.now());
                    notificationRepository.save(notification);
                });
    }

    public long getUnreadCount(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        return notificationCounterService.getUnreadCount(userId);
    }

    public long syncUnreadCount(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        return notificationCounterService.syncUnreadCount(userId);
    }

    @Transactional
    public void readNotification(UUID userId, UUID notificationId) {
        User user = userFinder.findProfileByUserId(userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found for ID: " + notificationId));

        userChecker.checkActive(user);
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Notification does not belong to the user.");
        }

        if (notification.getStatus() == NotificationStatus.READ) {
            throw new BadRequestException("Notification is already marked as read.");
        }

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
        notificationCounterService.decrement(userId);
    }

    @Transactional
    public void archiveNotification(UUID userId, UUID notificationId) {
        User user = userFinder.findProfileByUserId(userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found for ID: " + notificationId));

        userChecker.checkActive(user);
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Notification does not belong to the user.");
        }

        if (notification.getStatus() == NotificationStatus.ARCHIVED) {
            throw new BadRequestException("Notification is already archived.");
        }

        if (notification.getStatus() == NotificationStatus.UNREAD) {
            notificationCounterService.decrement(userId);
        }

        notification.setStatus(NotificationStatus.ARCHIVED);
        notificationRepository.save(notification);
    }

    @Transactional
    public void readAll(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        notificationRepository.markAllAsRead(userId, Instant.now());
        notificationCounterService.reset(userId);
    }

    @Transactional
    public void archiveAll(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        notificationRepository.markAllAsArchived(userId);
        notificationCounterService.reset(userId);
    }

    public void sendNotification(UUID userId, NotificationResponse notification) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );
    }
}
