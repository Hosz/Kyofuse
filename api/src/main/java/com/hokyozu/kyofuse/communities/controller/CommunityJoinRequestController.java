package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.response.CommunityJoinRequestResponse;
import com.hokyozu.kyofuse.communities.service.CommunityJoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/communities/join-requests")
@RequiredArgsConstructor
public class CommunityJoinRequestController {

    private final CommunityJoinRequestService communityJoinRequestService;

    @PostMapping("/{communityId}/request")
    public CommunityJoinRequestResponse requestToJoinCommunity(@AuthenticationPrincipal Jwt jwt,
                                                               @PathVariable UUID communityId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityJoinRequestService.requestToJoinCommunity(userId, communityId);
    }

    @PostMapping("/{requestId}/approve")
    public void approveJoinRequest(@AuthenticationPrincipal Jwt jwt,
                                                           @PathVariable UUID requestId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        communityJoinRequestService.approveJoinRequest(userId, requestId);
    }

    @DeleteMapping("/{requestId}/reject")
    public void rejectJoinRequest(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable UUID requestId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        communityJoinRequestService.rejectJoinRequest(userId, requestId);
    }
}
