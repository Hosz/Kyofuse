package com.hokyozu.kyofuse.reactions.mapper;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.PostReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostReactionMapperTest {

    @Test
    void mapsRequestToEntity() {
        Post post = Post.builder().id(UUID.randomUUID()).build();
        User user = User.builder().username("player").build();
        Instant before = Instant.now();

        PostReaction result = PostReactionMapper.toEntity(
                post,
                user,
                new PostReactionRequest(ReactionType.NICE_SHOT)
        );

        assertThat(result.getPost()).isSameAs(post);
        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getReactionType()).isEqualTo(ReactionType.NICE_SHOT);
        assertThat(result.getCreatedAt()).isBetween(before, Instant.now());
        assertThat(result.getUpdatedAt()).isBetween(before, Instant.now());
    }

    @Test
    void mapsEntityToResponse() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PostReaction reaction = PostReaction.builder()
                .post(Post.builder().id(postId).build())
                .user(User.builder().id(userId).username("player").build())
                .reactionType(ReactionType.FIRE)
                .build();
        GamerProfile profile = GamerProfile.builder()
                .nickname("PlayerNick")
                .avatarUrl("https://example.com/avatar.png")
                .build();

        PostReactionResponse result = PostReactionMapper.toResponse(reaction, profile);

        assertThat(result).isEqualTo(new PostReactionResponse(
                postId, userId, "player", "PlayerNick", "https://example.com/avatar.png", ReactionType.FIRE));
    }

    @Test
    void canInstantiateMapper() {
        assertThat(new PostReactionMapper()).isNotNull();
    }
}
