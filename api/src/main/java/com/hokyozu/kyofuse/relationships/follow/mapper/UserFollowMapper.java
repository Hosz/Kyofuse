package com.hokyozu.kyofuse.relationships.follow.mapper;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.relationships.follow.dto.response.UserFollowResponse;
import com.hokyozu.kyofuse.relationships.follow.entity.UserFollow;
import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class UserFollowMapper {
    public static UserFollow toInvite(User user, User followedUser) {
        return UserFollow.builder()
                .follower(user)
                .followed(followedUser)
                .status(FollowStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static UserFollowResponse toResponse(UserFollow userFollow, GamerProfile otherProfile) {
        return new UserFollowResponse(
                userFollow.getId(),
                userFollow.getFollower().getId(),
                userFollow.getFollower().getUsername(),
                userFollow.getFollowed().getId(),
                userFollow.getFollowed().getUsername(),
                userFollow.getStatus().name(),
                otherProfile.getAvatarUrl(),
                userFollow.getCreatedAt()
        );
    }

    public static UserFollow toFollow(User user, User followedUser) {
        return UserFollow.builder()
                .follower(user)
                .followed(followedUser)
                .status(FollowStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
