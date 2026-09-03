package com.hokyozu.kyofuse.notifications.service;

import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.dto.response.NotificationResponse;
import com.hokyozu.kyofuse.notifications.entity.Notification;
import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.mapper.NotificationMapper;
import com.hokyozu.kyofuse.notifications.repository.NotificationRepository;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserFinder userFinder;

    @Mock
    private UserChecker userChecker;

    @Mock
    private GamerProfileFinder gamerProfileFinder;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationCounterService notificationCounterService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotificationSavesAndIncrementsCounterAndSendsWebSocket() {
        UUID userId = UUID.randomUUID();
        User recipient = User.builder().id(userId).username("recipient").build();
        User actor = User.builder().id(UUID.randomUUID()).username("actor").build();

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipient(recipient)
                .actor(actor)
                .type(NotificationType.FOLLOW_STARTED)
                .title("Novo seguidor")
                .message("Você tem um novo seguidor")
                .targetType(NotificationTargetType.FOLLOW)
                .targetId(actor.getId())
                .build();

        Notification saved = Notification.builder()
                .id(UUID.randomUUID())
                .user(recipient)
                .actor(actor)
                .type(NotificationType.FOLLOW_STARTED)
                .status(NotificationStatus.UNREAD)
                .createdAt(Instant.now())
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(saved.getId())
                .type(saved.getType())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);
        when(notificationMapper.toResponse(saved)).thenReturn(response);

        Notification result = notificationService.createNotification(request);

        assertThat(result).isNotNull();
        verify(notificationCounterService).increment(userId);
        verify(messagingTemplate).convertAndSendToUser(eq(userId.toString()), eq("/queue/notifications"), eq(response));
    }

    @Test
    void readNotificationMarksAsReadAndDecrementsCounter() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Notification notification = Notification.builder()
                .id(notificationId)
                .user(user)
                .status(NotificationStatus.UNREAD)
                .createdAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.readNotification(userId, notificationId);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(notification.getReadAt()).isNotNull();
        verify(notificationRepository).save(notification);
        verify(notificationCounterService).decrement(userId);
    }

    @Test
    void readAllExecutesBulkUpdateAndResetsCounter() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        notificationService.readAll(userId);

        verify(notificationRepository).markAllAsRead(eq(userId), any(Instant.class));
        verify(notificationCounterService).reset(userId);
    }

    @Test
    void getUnreadCountReturnsCountFromCounterService() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(notificationCounterService.getUnreadCount(userId)).thenReturn(4L);

        long count = notificationService.getUnreadCount(userId);

        assertThat(count).isEqualTo(4L);
    }
}
