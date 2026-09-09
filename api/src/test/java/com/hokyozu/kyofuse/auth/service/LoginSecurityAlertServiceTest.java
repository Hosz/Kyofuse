package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.event.UserLoginSuccessEvent;
import com.hokyozu.kyofuse.infrastructure.client.DeviceInfo;
import com.hokyozu.kyofuse.infrastructure.client.UserAgentParser;
import com.hokyozu.kyofuse.infrastructure.geolocation.GeoLocationService;
import com.hokyozu.kyofuse.infrastructure.geolocation.LocationInfo;
import com.hokyozu.kyofuse.notifications.entity.Notification;
import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.mapper.NotificationMapper;
import com.hokyozu.kyofuse.notifications.repository.NotificationRepository;
import com.hokyozu.kyofuse.notifications.service.NotificationCounterService;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginSecurityAlertServiceTest {

    @Mock
    private GeoLocationService geoLocationService;

    @Mock
    private UserAgentParser userAgentParser;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationCounterService notificationCounterService;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private MailService mailService;

    @InjectMocks
    private LoginSecurityAlertService alertService;

    @Test
    void onUserLoginSuccessSavesNotificationAndSendsEmail() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("gamer123")
                .email("gamer123@example.com")
                .build();

        String ip = "189.40.10.20";
        String userAgent = "Mozilla/5.0 Chrome/128 Windows";
        Instant now = Instant.now();

        LocationInfo locationInfo = LocationInfo.of(ip, "Curitiba", "Paraná", "Brasil", "BR");
        DeviceInfo deviceInfo = new DeviceInfo("Chrome", "Windows", "Computador", "Chrome no Windows");

        when(geoLocationService.resolveLocation(ip)).thenReturn(locationInfo);
        when(userAgentParser.parse(userAgent)).thenReturn(deviceInfo);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        UserLoginSuccessEvent event = new UserLoginSuccessEvent(user, ip, userAgent, now);

        alertService.onUserLoginSuccess(event);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getActor()).isNull();
        assertThat(saved.getType()).isEqualTo(NotificationType.SYSTEM);
        assertThat(saved.getTitle()).isEqualTo("Novo login detectado");
        assertThat(saved.getMessage()).isEqualTo("Sua conta foi acessada em Curitiba, Paraná, Brasil usando Chrome no Windows.");
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(saved.getMetadataJson()).containsEntry("ip", ip);
        assertThat(saved.getMetadataJson()).containsEntry("location", "Curitiba, Paraná, Brasil");
        assertThat(saved.getMetadataJson()).containsEntry("device", "Chrome no Windows");

        verify(notificationCounterService).increment(user.getId());
        verify(messagingTemplate).convertAndSendToUser(eq(user.getId().toString()), eq("/queue/notifications"), any());

        verify(mailService).sendLoginSecurityAlertEmail(
                eq("gamer123@example.com"),
                eq("gamer123"),
                eq("Curitiba, Paraná, Brasil"),
                eq("Chrome no Windows"),
                eq(ip),
                eq(now)
        );
    }

    @Test
    void onUserLoginSuccessIgnoresNullEventOrNullUserSafely() {
        alertService.onUserLoginSuccess(null);
        alertService.onUserLoginSuccess(new UserLoginSuccessEvent(null, "127.0.0.1", null, Instant.now()));

        verifyNoInteractions(notificationRepository, mailService);
    }

    @Test
    void onUserLoginSuccessUsesPreResolvedLocationFromEventWithoutCallingGeoLocationService() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("gamer123")
                .email("gamer123@example.com")
                .build();

        String ip = "189.40.10.20";
        String userAgent = "Mozilla/5.0 Chrome/128 Windows";
        Instant now = Instant.now();

        LocationInfo locationInfo = LocationInfo.of(ip, "São Paulo", "São Paulo", "Brasil", "BR");
        DeviceInfo deviceInfo = new DeviceInfo("Chrome", "Windows", "Computador", "Chrome no Windows");

        when(userAgentParser.parse(userAgent)).thenReturn(deviceInfo);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        UserLoginSuccessEvent event = new UserLoginSuccessEvent(user, ip, userAgent, now, locationInfo);

        alertService.onUserLoginSuccess(event);

        verify(geoLocationService, never()).resolveLocation(anyString());
        verify(mailService).sendLoginSecurityAlertEmail(
                eq("gamer123@example.com"),
                eq("gamer123"),
                eq("São Paulo, Brasil"),
                eq("Chrome no Windows"),
                eq(ip),
                eq(now)
        );
    }
}
