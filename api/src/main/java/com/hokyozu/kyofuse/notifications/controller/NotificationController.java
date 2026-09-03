package com.hokyozu.kyofuse.notifications.controller;

import com.hokyozu.kyofuse.notifications.dto.response.NotificationResponse;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread-count")
    public java.util.Map<String, Long> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return java.util.Map.of("unreadCount", notificationService.getUnreadCount(userId));
    }

    @GetMapping
    public Page<NotificationResponse> listNotifications(@AuthenticationPrincipal Jwt jwt,
                                                        Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return notificationService.listNotifications(userId, pageable);
    }

    @PatchMapping("/{notificationId}/read")
    public void readNotification(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID notificationId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        notificationService.readNotification(userId, notificationId);
    }

    @PatchMapping("/{notificationId}/archive")
    public void archiveNotification(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID notificationId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        notificationService.archiveNotification(userId, notificationId);
    }

    @PatchMapping("/readall")
    public void readAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        notificationService.readAll(userId);
    }
}
