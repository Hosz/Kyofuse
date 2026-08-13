package com.hokyozu.kyofuse.posts.dto.response;

import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorId,
        String authorNickname,
        String authorUsername,
        String authorAvatarUrl,
        String content,
        PostType postType,
        PostVisibility postVisibility,
        PostStatus postStatus,
        Integer reactionCount,
        Integer likeCount,
        Integer commentCount,
        List<String> maps,
        Instant createdAt,
        Instant updatedAt
) {
}
