package com.hokyozu.kyofuse.relationships.follow.controller;

import com.hokyozu.kyofuse.relationships.follow.dto.response.UserFollowResponse;
import com.hokyozu.kyofuse.relationships.follow.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    @PostMapping("/{userFollowId}/follow")
    public UserFollowResponse followUser(@PathVariable UUID userFollowId,
                                         @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.followUser(userId, userFollowId);
    }

    @GetMapping("/{userIdFollowers}/followers")
    public Page<UserFollowResponse> showFollowers(@PathVariable UUID userIdFollowers,
                                                  @AuthenticationPrincipal Jwt jwt,
                                                  Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.showFollowers(userId, userIdFollowers, pageable);
    }

    @GetMapping("/me/followers")
    public Page<UserFollowResponse> showMyFollowers(@AuthenticationPrincipal Jwt jwt,
                                                    Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.showMyFollowers(userId, pageable);
    }

    @DeleteMapping("{followingId}/unfollow")
    public void unfollowUser(@PathVariable UUID followingId,
                             @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        userFollowService.unfollowUser(userId, followingId);
    }

    @GetMapping("/me/following")
    public Page<UserFollowResponse> showMyFollowings(@AuthenticationPrincipal Jwt jwt,
                                                     Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.showMyFollowings(userId, pageable);
    }

    @GetMapping("/{userIdFollowing}/following")
    public Page<UserFollowResponse> showFollowings(@PathVariable UUID userIdFollowing,
                                               @AuthenticationPrincipal Jwt jwt,
                                               Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.showFollowings(userId, userIdFollowing, pageable);
    }

    @DeleteMapping("/{userIdFollower}/delete")
    public void removeFollower(@PathVariable UUID userIdFollower,
                                                    @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        userFollowService.removeFollower(userId, userIdFollower);
    }

    @DeleteMapping("/{requestId}/reject")
    public void rejectFollowRequest(@PathVariable UUID requestId,
                              @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        userFollowService.rejectFollowRequest(userId, requestId);
    }

    @PatchMapping("/{requestId}/accept")
    public UserFollowResponse acceptFollowRequest(@PathVariable UUID requestId,
                              @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.acceptFollowRequest(userId, requestId);
    }
}
