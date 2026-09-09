package com.hokyozu.kyofuse.posts.dto.response;

import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;

import com.hokyozu.kyofuse.reactions.enums.ReactionType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorId,
        String authorNickname,
        String authorUsername,
        String authorAvatarUrl,
        UUID communityId,
        String communityName,
        String content,
        PostType postType,
        PostVisibility postVisibility,
        PostStatus postStatus,
        Integer reactionCount,
        Integer likeCount,
        Integer commentCount,
        Long viewCount,
        List<String> maps,
        List<PostMediaResponse> media,
        ReactionType currentUserReaction,
        Instant createdAt,
        Instant updatedAt
) {
    public PostResponse(UUID id, UUID authorId, String authorNickname, String authorUsername, String authorAvatarUrl, UUID communityId, String communityName, String content, PostType postType, PostVisibility postVisibility, PostStatus postStatus, Integer reactionCount, Integer likeCount, Integer commentCount, Long viewCount, List<String> maps, List<PostMediaResponse> media, Instant createdAt, Instant updatedAt) {
        this(id, authorId, authorNickname, authorUsername, authorAvatarUrl, communityId, communityName, content, postType, postVisibility, postStatus, reactionCount, likeCount, commentCount, viewCount, maps, media, null, createdAt, updatedAt);
    }

    public PostResponse(UUID id, UUID authorId, String authorNickname, String authorUsername, String authorAvatarUrl, UUID communityId, String communityName, String content, PostType postType, PostVisibility postVisibility, PostStatus postStatus, Integer reactionCount, Integer likeCount, Integer commentCount, List<String> maps, List<PostMediaResponse> media, Instant createdAt, Instant updatedAt) {
        this(id, authorId, authorNickname, authorUsername, authorAvatarUrl, communityId, communityName, content, postType, postVisibility, postStatus, reactionCount, likeCount, commentCount, 0L, maps, media, null, createdAt, updatedAt);
    }

    public PostResponse(UUID id, UUID authorId, String authorNickname, String authorUsername, String authorAvatarUrl, UUID communityId, String communityName, String content, PostType postType, PostVisibility postVisibility, PostStatus postStatus, Integer reactionCount, Integer likeCount, Integer commentCount, List<String> maps, Instant createdAt, Instant updatedAt) {
        this(id, authorId, authorNickname, authorUsername, authorAvatarUrl, communityId, communityName, content, postType, postVisibility, postStatus, reactionCount, likeCount, commentCount, 0L, maps, List.of(), null, createdAt, updatedAt);
    }
}
