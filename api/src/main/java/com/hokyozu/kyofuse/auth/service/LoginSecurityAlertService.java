package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.event.UserLoginSuccessEvent;
import com.hokyozu.kyofuse.infrastructure.client.DeviceInfo;
import com.hokyozu.kyofuse.infrastructure.client.UserAgentParser;
import com.hokyozu.kyofuse.infrastructure.geolocation.GeoLocationService;
import com.hokyozu.kyofuse.infrastructure.geolocation.LocationInfo;
import com.hokyozu.kyofuse.notifications.entity.Notification;
import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.mapper.NotificationMapper;
import com.hokyozu.kyofuse.notifications.repository.NotificationRepository;
import com.hokyozu.kyofuse.notifications.service.NotificationCounterService;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSecurityAlertService {

    private final GeoLocationService geoLocationService;
    private final UserAgentParser userAgentParser;
    private final NotificationRepository notificationRepository;
    private final NotificationCounterService notificationCounterService;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final MailService mailService;
    private final UserSessionService userSessionService;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserLoginSuccess(UserLoginSuccessEvent event) {
        if (event == null || event.user() == null) {
            return;
        }

        try {
            User user = event.user();
            String deviceId = event.deviceId();

            if (deviceId != null && !deviceId.isBlank() && userSessionService != null && user.getId() != null) {
                if (userSessionService.isDeviceTrusted(user.getId(), deviceId)) {
                    log.info("[LoginSecurity] Dispositivo {} confiável para usuário {}. Notificação e e-mail suprimidos.", deviceId, user.getUsername());
                    return;
                }
            }
            String clientIp = event.clientIp();
            String userAgent = event.userAgent();
            Instant loggedAt = event.loggedAt() != null ? event.loggedAt() : Instant.now();

            LocationInfo location = (event.location() != null)
                    ? event.location()
                    : geoLocationService.resolveLocation(clientIp);
            DeviceInfo device = userAgentParser.parse(userAgent);

            String formattedLocation = location.formattedLocation();
            String deviceSummary = device.summary();

            String title = "Novo login detectado";
            String message = String.format("Sua conta foi acessada em %s usando %s.", formattedLocation, deviceSummary);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("ip", clientIp != null ? clientIp : "desconhecido");
            metadata.put("location", formattedLocation);
            if (location.city() != null) metadata.put("city", location.city());
            if (location.state() != null) metadata.put("state", location.state());
            if (location.country() != null) metadata.put("country", location.country());
            metadata.put("device", deviceSummary);
            metadata.put("browser", device.browser());
            metadata.put("os", device.operatingSystem());
            metadata.put("loggedAt", loggedAt.toString());

            Notification notification = Notification.builder()
                    .user(user)
                    .actor(null)
                    .type(NotificationType.SYSTEM)
                    .title(title)
                    .message(message)
                    .status(NotificationStatus.UNREAD)
                    .targetType(NotificationTargetType.SYSTEM)
                    .metadataJson(metadata)
                    .createdAt(loggedAt)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("[LoginSecurity] Notificação in-app de login criada para usuário: {}", user.getUsername());

            if (notificationCounterService != null && user.getId() != null) {
                notificationCounterService.increment(user.getId());
            }

            if (messagingTemplate != null && notificationMapper != null && user.getId() != null) {
                try {
                    messagingTemplate.convertAndSendToUser(
                            user.getId().toString(),
                            "/queue/notifications",
                            notificationMapper.toResponse(saved)
                    );
                } catch (Exception wsEx) {
                    log.warn("[LoginSecurity] Falha ao enviar notificação de login via WebSocket: {}", wsEx.getMessage());
                }
            }

            mailService.sendLoginSecurityAlertEmail(
                    user.getEmail(),
                    user.getUsername(),
                    formattedLocation,
                    deviceSummary,
                    clientIp,
                    loggedAt
            );
        } catch (Exception e) {
            log.error("[LoginSecurity] Erro ao processar notificação de segurança de login: {}", e.getMessage(), e);
        }
    }
}
