package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/create")
    public CommunityResponse createCommunity(@AuthenticationPrincipal Jwt jwt,
                                             @RequestBody @Valid CommunityRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.createCommunity(request, userId);
    }

    @PatchMapping("/edit/{communityId}")
    public CommunityResponse editCommunity(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable UUID communityId,
                                           @RequestBody @Valid UpdateCommunityRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.editCommunity(userId, communityId, request);
    }

    @GetMapping("/{communityId}")
    public CommunityResponse detailCommunity(@PathVariable UUID communityId) {
        return communityService.detailCommunity(communityId);
    }

    @DeleteMapping("/{communityId}")
    public void deleteCommunity(@AuthenticationPrincipal Jwt jwt,
                                @PathVariable UUID communityId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        communityService.deleteCommunity(userId, communityId);
    }

    @PatchMapping("/{communityId}/archive")
    public void archiveCommunity(@AuthenticationPrincipal Jwt jwt,
                                 @PathVariable UUID communityId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        communityService.archiveCommunity(userId, communityId);
    }
}
