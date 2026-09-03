package com.hokyozu.kyofuse.presence.controller;

import com.hokyozu.kyofuse.presence.dto.request.BatchPresenceRequest;
import com.hokyozu.kyofuse.presence.dto.response.PresenceResponse;
import com.hokyozu.kyofuse.presence.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/presence", "/api/v1/presence"})
@RequiredArgsConstructor
public class PresenceController {

    private final UserPresenceService userPresenceService;

    @GetMapping("/{userId}")
    public ResponseEntity<PresenceResponse> getPresence(@PathVariable UUID userId) {
        return ResponseEntity.ok(userPresenceService.getPresence(userId));
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<UUID, PresenceResponse>> getPresenceBatch(@RequestBody BatchPresenceRequest request) {
        return ResponseEntity.ok(userPresenceService.getPresenceBatch(request.userIds()));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(Principal principal) {
        if (principal != null) {
            UUID userId = UUID.fromString(principal.getName());
            userPresenceService.heartbeat(userId);
        }
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/presence/heartbeat")
    public void handleHeartbeat(Principal principal) {
        if (principal != null) {
            UUID userId = UUID.fromString(principal.getName());
            userPresenceService.heartbeat(userId);
        }
    }
}
