package com.hokyozu.kyofuse.relationships.follow.controller;

import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.relationships.follow.dto.response.UserFollowResponse;
import com.hokyozu.kyofuse.relationships.follow.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/{userIdFollowers}/followers/quantity")
    public Long showFollowersQuantity(@PathVariable UUID userIdFollowers) {
        return userFollowService.showFollowersQuantity(userIdFollowers);
    }

    @GetMapping("/me/followers")
    public Page<UserFollowResponse> showMyFollowers(@AuthenticationPrincipal Jwt jwt,
                                                    Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.showMyFollowers(userId, pageable);
    }

    @GetMapping("/me/followers/quantity")
    public Long showMyFollowersQuantity(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.showMyFollowersQuantity(userId);
    }

    @GetMapping("/{otherUserId}/is-following")
    public boolean isFollowing(@PathVariable UUID otherUserId,
                               @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.isFollowing(userId, otherUserId);
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

    @GetMapping("/me/following/quantity")
    public Long showMyFollowingsQuantity(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.showMyFollowingsQuantity(userId);
    }

    @GetMapping("/{userIdFollowing}/following")
    public Page<UserFollowResponse> showFollowings(@PathVariable UUID userIdFollowing,
                                               @AuthenticationPrincipal Jwt jwt,
                                               Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.showFollowings(userId, userIdFollowing, pageable);
    }

    @GetMapping("/{userIdFollowing}/following/quantity")
    public Long showFollowingQuantity(@PathVariable UUID userIdFollowing) {
        return userFollowService.showFollowingQuantity(userIdFollowing);
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

    @GetMapping("/suggestions")
    public Page<GamerProfileResponse> getFollowSuggestions(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 5) Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userFollowService.getFollowSuggestions(userId, pageable);
    }
}
