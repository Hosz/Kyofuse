package com.hokyozu.kyofuse.notifications.service;

import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.dto.response.NotificationResponse;
import com.hokyozu.kyofuse.notifications.entity.Notification;
import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.mapper.NotificationMapper;
import com.hokyozu.kyofuse.notifications.repository.NotificationRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserFinder userFinder;
    private final UserChecker userChecker;

    @Transactional
    public Notification createNotification(CreateNotificationRequest request) {
        Notification notification = NotificationMapper.toEntity(request);
        return notificationRepository.save(notification);
    }

    public Page<NotificationResponse> listNotifications(
            UUID userId,
            Pageable pageable) {

        return notificationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse);
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

        notification.setStatus(NotificationStatus.ARCHIVED);
        notificationRepository.save(notification);
    }

    @Transactional
    public void readAll(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        List<Notification> notifications = notificationRepository.findAllByUserId(userId);
        notifications.forEach(notification -> {
            if (notification.getStatus() == NotificationStatus.UNREAD) {
                notification.setStatus(NotificationStatus.READ);
                notification.setReadAt(Instant.now());
            }
        });

        notificationRepository.saveAll(notifications);
    }
}
