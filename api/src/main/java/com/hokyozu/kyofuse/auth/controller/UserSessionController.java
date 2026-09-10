package com.hokyozu.kyofuse.auth.controller;

import com.hokyozu.kyofuse.auth.dto.response.UserSessionResponse;
import com.hokyozu.kyofuse.auth.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/sessions")
@RequiredArgsConstructor
public class UserSessionController {

    private final UserSessionService userSessionService;

    @GetMapping
    public List<UserSessionResponse> listActiveSessions(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String deviceId = request.getHeader("X-Device-Id");
        return userSessionService.listActiveSessions(userId, deviceId);
    }

    @PatchMapping("/trust")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trustCurrentDevice(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String deviceId = request.getHeader("X-Device-Id");
        userSessionService.trustDevice(userId, deviceId);
    }

    @PatchMapping("/{sessionId}/untrust")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void untrustDevice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID sessionId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userSessionService.untrustDevice(userId, sessionId);
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeSession(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID sessionId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userSessionService.revokeSession(userId, sessionId);
    }

    @DeleteMapping("/others")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeAllOtherSessions(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String deviceId = request.getHeader("X-Device-Id");
        userSessionService.revokeAllOtherSessions(userId, deviceId);
    }
}
