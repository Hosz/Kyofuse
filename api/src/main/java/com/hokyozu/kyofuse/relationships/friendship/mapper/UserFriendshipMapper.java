package com.hokyozu.kyofuse.relationships.friendship.mapper;

import com.hokyozu.kyofuse.relationships.friendship.dto.response.UserFriendshipResponse;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendship;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class UserFriendshipMapper {

    public static UserFriendship toEntity(User userOne, User userTwo) {
        return UserFriendship.builder()
                .userOne(userOne)
                .userTwo(userTwo)
                .createdAt(Instant.now())
                .build();
    }

    public static UserFriendshipResponse toResponse(UserFriendship userFriendship) {
        return new UserFriendshipResponse(
                userFriendship.getUserOne().getId(),
                userFriendship.getUserOne().getUsername(),
                userFriendship.getUserTwo().getId(),
                userFriendship.getUserTwo().getUsername(),
                userFriendship.getCreatedAt()
        );
    }
}
