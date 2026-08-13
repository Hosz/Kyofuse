package com.hokyozu.kyofuse.relationships.friendship.controller;

import com.hokyozu.kyofuse.relationships.friendship.dto.response.UserFriendshipResponse;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendship;
import com.hokyozu.kyofuse.relationships.friendship.service.UserFriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-friendship")
@RequiredArgsConstructor
public class UserFriendshipController {

    private final UserFriendshipService userFriendshipService;

    @PostMapping("/{requestId}/accept-request")
    public UserFriendshipResponse acceptRequest(@AuthenticationPrincipal Jwt jwt,
                                                @PathVariable UUID requestId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFriendshipService.acceptRequest(userId, requestId);
    }

    @DeleteMapping("/{requestId}/decline-request")
    public void declineRequest(@AuthenticationPrincipal Jwt jwt,
                            @PathVariable UUID requestId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        userFriendshipService.declineRequest(userId, requestId);
    }

    @GetMapping("/me/friends")
    public Page<UserFriendshipResponse> showMyFriends(@AuthenticationPrincipal Jwt jwt,
                                              Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFriendshipService.showMyFriends(userId, pageable);
    }

    @GetMapping("/{userId}/friends")
    public Page<UserFriendshipResponse> showUserFriends(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable UUID userId,
                                                        Pageable pageable) {

        UUID userAuthId = UUID.fromString(jwt.getSubject());
        return userFriendshipService.showUserFriends(userAuthId, userId, pageable);
    }

    @DeleteMapping("/{friendId}/remove")
    public void removeFriendship(@AuthenticationPrincipal Jwt jwt,
                                 @PathVariable UUID friendId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        userFriendshipService.removeFriendship(userId, friendId);
    }

    @GetMapping("/me/friends/quantity")
    public long showMyFriendsQuantity(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userFriendshipService.showMyFriendsQuantity(userId);
    }

    @GetMapping("/{userId}/friends/quantity")
    public long showUserFriendsQuantity(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable UUID userId) {
        UUID userAuthId = UUID.fromString(jwt.getSubject());
        return userFriendshipService.showUserFriendsQuantity(userAuthId, userId);
    }
}
