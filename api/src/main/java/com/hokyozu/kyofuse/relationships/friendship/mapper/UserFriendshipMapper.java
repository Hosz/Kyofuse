package com.hokyozu.kyofuse.relationships.friendship.mapper;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.relationships.friendship.dto.response.UserFriendshipResponse;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendship;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;
import java.util.UUID;

public class UserFriendshipMapper {

    public static UserFriendship toEntity(User userOne, User userTwo) {
        return UserFriendship.builder()
                .userOne(userOne)
                .userTwo(userTwo)
                .createdAt(Instant.now())
                .build();
    }

    public static User resolveFriend(UserFriendship userFriendship, UUID viewerId) {
        return userFriendship.getUserOne().getId().equals(viewerId)
                ? userFriendship.getUserTwo()
                : userFriendship.getUserOne();
    }

    public static UserFriendshipResponse toResponse(UserFriendship userFriendship, UUID viewerId, GamerProfile gamerProfile) {
        User friend = resolveFriend(userFriendship, viewerId);

        return new UserFriendshipResponse(
                friend.getId(),
                friend.getUsername(),
                gamerProfile.getAvatarUrl(),
                userFriendship.getCreatedAt()
        );
    }
}
