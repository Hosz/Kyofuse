package com.hokyozu.kyofuse.posts.mapper;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostMapperTest {

    @Test
    void defaultConstructorCanBeCreated() {
        assertThat(new PostMapper()).isNotNull();
    }

    @Test
    void toEntityAndToResponseMapPostFields() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        GamerProfile profile = GamerProfile.builder().user(user).build();
        CreatePostRequest request = new CreatePostRequest("content", PostType.TEXT, PostVisibility.PUBLIC);

        Post post = PostMapper.toEntity(profile, request);
        post.setId(postId);
        post.setReactionCount(3);
        post.setLikeCount(2);
        post.setCommentCount(1);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());

        PostResponse response = PostMapper.toResponse(post);

        assertThat(post.getAuthor()).isSameAs(user);
        assertThat(post.getStatus()).isEqualTo(PostStatus.ACTIVE);
        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(userId);
        assertThat(response.reactionCount()).isEqualTo(3);
        assertThat(response.likeCount()).isEqualTo(2);
        assertThat(response.commentCount()).isEqualTo(1);
    }
}
