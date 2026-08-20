package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.service.UserPinnedCommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/communities/pinned")
@RequiredArgsConstructor
public class UserPinnedCommunityController {

    private final UserPinnedCommunityService userPinnedCommunityService;

    @GetMapping
    public List<CommunityResponse> listPinnedCommunities(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userPinnedCommunityService.listPinnedCommunities(userId);
    }

    @PostMapping("/{communityId}")
    public CommunityResponse pinCommunity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID communityId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userPinnedCommunityService.pinCommunity(userId, communityId);
    }

    @PutMapping("/order")
    public List<CommunityResponse> reorderPinnedCommunities(@AuthenticationPrincipal Jwt jwt,
                                                            @RequestBody List<UUID> communityIds) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userPinnedCommunityService.reorderPinnedCommunities(userId, communityIds);
    }

    @DeleteMapping("/{communityId}")
    public void unpinCommunity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID communityId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userPinnedCommunityService.unpinCommunity(userId, communityId);
    }
}
