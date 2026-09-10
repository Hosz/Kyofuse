package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.request.CommunityInviteCancelRequest;
import com.hokyozu.kyofuse.communities.dto.request.CommunityInviteRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityInviteResponse;
import com.hokyozu.kyofuse.communities.enums.CommunityInviteStatus;
import com.hokyozu.kyofuse.communities.service.CommunityInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/community-invite")
@RequiredArgsConstructor
public class CommunityInviteController {

    private final CommunityInviteService communityInviteService;

    @PostMapping("/{communityIdentifier}/invite/{receiverUsername}")
    public CommunityInviteResponse inviteUser(@PathVariable String communityIdentifier,
                                              @PathVariable String receiverUsername,
                                              @AuthenticationPrincipal Jwt jwt,
                                              @Valid @RequestBody(required = false) CommunityInviteRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return communityInviteService.inviteUser(userId, communityIdentifier, receiverUsername, request);
    }

    @GetMapping("/{communityIdentifier}/invites")
    public Page<CommunityInviteResponse> listInvites(@PathVariable String communityIdentifier,
                                                     @AuthenticationPrincipal Jwt jwt,
                                                     Pageable pageable,
                                                     @RequestParam(required = false) CommunityInviteStatus status) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return communityInviteService.listInvites(userId, communityIdentifier, pageable, status);
    }

    @PatchMapping("/{inviteId}/accept")
    public void acceptInvite(@PathVariable UUID inviteId,
                             @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        communityInviteService.acceptInvite(userId, inviteId);
    }

    @PatchMapping("/{inviteId}/decline")
    public void declineInvite(@PathVariable UUID inviteId,
                              @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        communityInviteService.declineInvite(userId, inviteId);
    }

    @PatchMapping("/{inviteId}/cancel")
    public void cancelInvite(@PathVariable UUID inviteId,
                             @AuthenticationPrincipal Jwt jwt,
                             @Valid @RequestBody(required = false) CommunityInviteCancelRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        communityInviteService.cancelInvite(userId, inviteId, request);
    }
}
