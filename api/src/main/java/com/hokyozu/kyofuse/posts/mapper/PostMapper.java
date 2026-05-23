package com.hokyozu.kyofuse.posts.mapper;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;

import java.time.Instant;

public class PostMapper {

    public static Post toEntity(GamerProfile profile, CreatePostRequest request) {
        return Post.builder()
                .author(profile.getUser())
                .content(request.content())
                .postType(request.postType())
                .visibility(request.visibility())
                .status(PostStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static PostResponse toResponse(Post savedPost) {
        return new PostResponse(
                savedPost.getId(),
                savedPost.getAuthor().getId(),
                savedPost.getContent(),
                savedPost.getPostType(),
                savedPost.getVisibility(),
                savedPost.getStatus(),
                savedPost.getReactionCount(),
                savedPost.getLikeCount(),
                savedPost.getCommentCount(),
                savedPost.getCreatedAt(),
                savedPost.getUpdatedAt()
        );
    }
}
