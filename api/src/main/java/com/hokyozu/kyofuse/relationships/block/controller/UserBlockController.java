package com.hokyozu.kyofuse.relationships.block.controller;

import com.hokyozu.kyofuse.relationships.block.dto.response.UserBlockResponse;
import com.hokyozu.kyofuse.relationships.block.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-block")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockService userBlockService;

    @PostMapping("/{userBlockId}/block")
    public UserBlockResponse blockUser(@PathVariable UUID userBlockId,
                                       @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userBlockService.blockUser(userId, userBlockId);
    }

    @GetMapping("/blocked-users")
    public Page<UserBlockResponse> getBlockedUsers(@AuthenticationPrincipal Jwt jwt,
                                                   Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userBlockService.getBlockedUsers(userId, pageable);
    }

    @DeleteMapping("/unblock/{userBlockId}")
    public void unblockUser(@PathVariable UUID userBlockId,
                            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        userBlockService.unblockUser(userId, userBlockId);
    }
}
