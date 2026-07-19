package com.hokyozu.kyofuse.relationships.friendship.mapper;

import com.hokyozu.kyofuse.relationships.friendship.dto.response.UserFriendRequestResponse;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendRequest;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class UserFriendRequestMapper {

    public static UserFriendRequest sendRequest(User user, User userFriendRequest) {
        return UserFriendRequest.builder()
                .sender(user)
                .receiver(userFriendRequest)
                .createdAt(Instant.now())
                .build();
    }

    public static UserFriendRequestResponse toResponse(UserFriendRequest friendRequest) {
        return new UserFriendRequestResponse(
                friendRequest.getSender().getId(),
                friendRequest.getSender().getUsername(),
                friendRequest.getReceiver().getId(),
                friendRequest.getReceiver().getUsername(),
                friendRequest.getCreatedAt()
        );
    }
}
