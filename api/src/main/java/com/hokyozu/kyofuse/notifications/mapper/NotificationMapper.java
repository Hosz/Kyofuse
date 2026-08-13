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
                .user(request.actor())
                .type(request.type())
                .title(request.title())
                .message(request.message())
                .status(NotificationStatus.UNREAD)
                .actor(request.actor())
                .targetType(request.targetType())
                .targetId(request.targetId())
                .metadataJson(request.metadata())
                .createdAt(Instant.now())
                .build();
    }

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .actor(toActor(notification.getActor()))
                .target(toTarget(notification))
                .metadata(notification.getMetadataJson())
                .build();
    }

    private NotificationActorResponse toActor(User actor) {
        if (actor == null) {
            return null;
        }
        GamerProfile gamerProfile = gamerProfileRepository.findByUserId(actor.getId())
                .orElseThrow(() -> new NotFoundException("Gamer profile not found for user ID: " + actor.getId()));

        return NotificationActorResponse.builder()
                .username(actor.getUsername())
                .avatarUrl(gamerProfile.getAvatarUrl())
                .build();
    }

    private NotificationTargetResponse toTarget(Notification notification) {

        return NotificationTargetResponse.builder()
                .type(notification.getTargetType())
                .id(notification.getTargetId())
                .build();
    }
}
