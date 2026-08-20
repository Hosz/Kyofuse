package com.hokyozu.kyofuse.reactions.mapper;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.reactions.dto.request.CommentReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.CommentReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.CommentReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentReactionMapperTest {

    @Test
    void mapsRequestToEntity() {
        Comment comment = Comment.builder().id(UUID.randomUUID()).build();
        User user = User.builder().username("player").build();
        Instant before = Instant.now();

        CommentReaction result = CommentReactionMapper.toEntity(
                comment, user, new CommentReactionRequest(ReactionType.NICE_SHOT));

        assertThat(result.getComment()).isSameAs(comment);
        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getReactionType()).isEqualTo(ReactionType.NICE_SHOT);
        assertThat(result.getCreatedAt()).isBetween(before, Instant.now());
        assertThat(result.getUpdatedAt()).isBetween(before, Instant.now());
    }

    @Test
    void mapsEntityToResponse() {
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Comment comment = Comment.builder()
                .id(commentId)
                .post(Post.builder().id(postId).build())
                .build();
        CommentReaction reaction = CommentReaction.builder()
                .comment(comment)
                .user(User.builder().id(userId).username("player").build())
                .reactionType(ReactionType.FIRE)
                .build();
        GamerProfile profile = GamerProfile.builder()
                .nickname("PlayerNick")
                .avatarUrl("https://example.com/avatar.png")
                .build();

        CommentReactionResponse result = CommentReactionMapper.toResponse(reaction, profile);

        assertThat(result).isEqualTo(
                new CommentReactionResponse(postId, commentId, userId, "player", "PlayerNick",
                        "https://example.com/avatar.png", ReactionType.FIRE));
    }

    @Test
    void canInstantiateMapper() {
        assertThat(new CommentReactionMapper()).isNotNull();
    }
}
