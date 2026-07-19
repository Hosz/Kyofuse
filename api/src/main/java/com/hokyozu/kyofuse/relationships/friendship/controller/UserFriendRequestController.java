package com.hokyozu.kyofuse.relationships.friendship.controller;

import com.hokyozu.kyofuse.relationships.friendship.dto.response.UserFriendRequestResponse;
import com.hokyozu.kyofuse.relationships.friendship.service.UserFriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-friend-request")
@RequiredArgsConstructor
public class UserFriendRequestController {

    private final UserFriendRequestService userFriendRequestService;

    @PostMapping("/{userFriendRequestId}/send")
    public UserFriendRequestResponse sendRequest(@AuthenticationPrincipal Jwt jwt,
                                                 @PathVariable UUID userFriendRequestId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userFriendRequestService.sendRequest(userId, userFriendRequestId);
    }

    @GetMapping("/requests-recieved")
    public Page<UserFriendRequestResponse> showRequests(@AuthenticationPrincipal Jwt jwt,
                                                        Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFriendRequestService.showRequests(userId, pageable);
    }

    @GetMapping("/requests-sent")
    public Page<UserFriendRequestResponse> showSentRequests(@AuthenticationPrincipal Jwt jwt,
                                                              Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFriendRequestService.showSentRequests(userId, pageable);
    }

    @DeleteMapping("/{requestId}/remove-request")
    public void removeRequest(@AuthenticationPrincipal Jwt jwt,
                              @PathVariable UUID requestId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        userFriendRequestService.removeRequest(userId, requestId);
    }

}
