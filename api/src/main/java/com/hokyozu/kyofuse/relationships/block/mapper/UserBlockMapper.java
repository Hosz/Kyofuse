package com.hokyozu.kyofuse.relationships.block.mapper;

import com.hokyozu.kyofuse.relationships.block.dto.response.UserBlockResponse;
import com.hokyozu.kyofuse.relationships.block.entity.UserBlock;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class UserBlockMapper {
    public static UserBlock toEntity(User user, User userBlocked) {
        return UserBlock.builder()
                .blocker(user)
                .blocked(userBlocked)
                .createdAt(Instant.now())
                .build();
    }

    public static UserBlockResponse toResponse(UserBlock block) {
        return new UserBlockResponse(
                "Blocked",
                block.getBlocker().getId(),
                block.getBlocker().getUsername(),
                block.getBlocked().getId(),
                block.getBlocked().getUsername(),
                block.getCreatedAt()
        );
    }
}
