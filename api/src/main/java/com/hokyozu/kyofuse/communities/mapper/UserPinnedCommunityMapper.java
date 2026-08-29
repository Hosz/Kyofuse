package com.hokyozu.kyofuse.communities.mapper;

import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.UserPinnedCommunity;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class UserPinnedCommunityMapper {

    private UserPinnedCommunityMapper() {}

    public static UserPinnedCommunity toEntity(User user, Community community, int position) {
        return UserPinnedCommunity.builder()
                .user(user)
                .community(community)
                .position(position)
                .createdAt(Instant.now())
                .build();
    }
}
