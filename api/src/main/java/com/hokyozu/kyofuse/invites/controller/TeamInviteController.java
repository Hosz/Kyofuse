package com.hokyozu.kyofuse.invites.controller;

import com.hokyozu.kyofuse.invites.dto.request.TeamInviteCancelRequest;
import com.hokyozu.kyofuse.invites.dto.request.TeamInviteRequest;
import com.hokyozu.kyofuse.invites.dto.response.TeamInviteResponse;
import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.invites.service.TeamInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/team-invite")
@RequiredArgsConstructor
public class TeamInviteController {

    private final TeamInviteService teamInviteService;

    @PostMapping("/{teamId}/invite/{receiverUsername}")
    public TeamInviteResponse inviteUser(@PathVariable UUID teamId,
                                         @PathVariable String receiverUsername,
                                         @AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody TeamInviteRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return teamInviteService.inviteUser(userId, teamId, receiverUsername, request);
    }

    @GetMapping("/{teamId}/invites")
    public Page<TeamInviteResponse> listInvites(@PathVariable UUID teamId,
                                                @AuthenticationPrincipal Jwt jwt,
                                                Pageable pageable,
                                                @RequestParam(required = false) TeamInviteStatus status) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return teamInviteService.listInvites(userId, teamId, pageable, status);
    }

    @PatchMapping("/{inviteId}/accept")
    public void acceptInvite(@PathVariable UUID inviteId,
                             @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        teamInviteService.acceptInvite(userId, inviteId);
    }

    @PatchMapping("/{inviteId}/decline")
    public void declineInvite(@PathVariable UUID inviteId,
                              @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        teamInviteService.declineInvite(userId, inviteId);
    }

    @PatchMapping("/{inviteId}/cancel")
    public void cancelInvite(@PathVariable UUID inviteId,
                             @AuthenticationPrincipal Jwt jwt,
                             @Valid @RequestBody TeamInviteCancelRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        teamInviteService.cancelInvite(userId, inviteId, request);
    }
}
