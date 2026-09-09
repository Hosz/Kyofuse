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
import com.hokyozu.kyofuse.notifications.repository.NotificationRepository;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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
    private final MailService mailService;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserLoginSuccess(UserLoginSuccessEvent event) {
        if (event == null || event.user() == null) {
            return;
        }

        try {
            User user = event.user();
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

            notificationRepository.save(notification);
            log.info("[LoginSecurity] Notificação in-app de login criada para usuário: {}", user.getUsername());

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
