package com.hokyozu.kyofuse.reactions.mapper;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.PostReaction;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;

import java.time.Instant;

public class PostReactionMapper {
    public static PostReactionResponse toResponse(PostReaction postReactionSaved, GamerProfile profile) {
        return new PostReactionResponse(
                postReactionSaved.getPost().getId(),
                postReactionSaved.getUser().getId(),
                postReactionSaved.getUser().getUsername(),
                profile.getNickname(),
                profile.getAvatarUrl(),
                postReactionSaved.getReactionType()
        );
    }

    public static PostReaction toEntity(Post post, User user, @Valid PostReactionRequest request) {
        return PostReaction.builder()
                .post(post)
                .user(user)
                .reactionType(request.reactionType())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
