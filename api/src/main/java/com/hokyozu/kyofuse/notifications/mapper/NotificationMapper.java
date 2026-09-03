package com.hokyozu.kyofuse.notifications.mapper;

import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.dto.response.NotificationActorResponse;
import com.hokyozu.kyofuse.notifications.dto.response.NotificationResponse;
import com.hokyozu.kyofuse.notifications.dto.response.NotificationTargetResponse;
import com.hokyozu.kyofuse.notifications.entity.Notification;
import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final GamerProfileRepository gamerProfileRepository;

    public static Notification toEntity(CreateNotificationRequest request) {
        return Notification.builder()
                .user(request.recipient())
                .actor(request.actor())
                .type(request.type())
                .title(request.title())
                .message(request.message())
                .status(NotificationStatus.UNREAD)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .metadataJson(request.metadata())
                .createdAt(Instant.now())
                .build();
    }

    public NotificationResponse toResponse(Notification notification) {
        return toResponse(notification, notification.getActor() != null ? gamerProfileRepository.findByUserId(notification.getActor().getId()).orElse(null) : null);
    }

    public NotificationResponse toResponse(Notification notification, GamerProfile actorProfile) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .actor(toActor(notification.getActor(), actorProfile))
                .target(toTarget(notification))
                .metadata(notification.getMetadataJson())
                .build();
    }

    private NotificationActorResponse toActor(User actor, GamerProfile actorProfile) {
        if (actor == null) {
            return null;
        }

        return NotificationActorResponse.builder()
                .username(actor.getUsername())
                .avatarUrl(actorProfile != null ? actorProfile.getAvatarUrl() : null)
                .build();
    }

    private NotificationTargetResponse toTarget(Notification notification) {

        return NotificationTargetResponse.builder()
                .type(notification.getTargetType())
                .id(notification.getTargetId())
                .build();
    }
}
