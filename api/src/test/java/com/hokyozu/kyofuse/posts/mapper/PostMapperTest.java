package com.hokyozu.kyofuse.posts.mapper;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.entity.PostMap;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
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
        CreatePostRequest request = new CreatePostRequest(
                "content",
                PostType.TEXT,
                PostVisibility.PUBLIC,
                List.of(Cs2Map.MIRAGE)
        );

        Post post = PostMapper.toEntity(profile, request);
        post.setId(postId);
        post.setReactionCount(3);
        post.setLikeCount(2);
        post.setCommentCount(1);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        List<PostMap> postMaps = List.of(
                PostMap.builder().post(post).mapName("MIRAGE").createdAt(Instant.now()).build(),
                PostMap.builder().post(post).mapName("INFERNO").createdAt(Instant.now()).build()
        );

        PostResponse response = PostMapper.toResponse(post, postMaps);

        assertThat(post.getAuthor()).isSameAs(user);
        assertThat(post.getStatus()).isEqualTo(PostStatus.ACTIVE);
        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(userId);
        assertThat(response.reactionCount()).isEqualTo(3);
        assertThat(response.likeCount()).isEqualTo(2);
        assertThat(response.commentCount()).isEqualTo(1);
        assertThat(response.maps()).containsExactly("MIRAGE", "INFERNO");
    }

    @Test
    void toResponseUsesEmptyMapsWhenPostMapsIsNull() {
        UUID userId = UUID.randomUUID();
        Post post = Post.builder()
                .id(UUID.randomUUID())
                .author(User.builder().id(userId).build())
                .content("content")
                .postType(PostType.TEXT)
                .visibility(PostVisibility.PUBLIC)
                .status(PostStatus.ACTIVE)
                .reactionCount(0)
                .likeCount(0)
                .commentCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        PostResponse response = PostMapper.toResponse(post, null);

        assertThat(response.maps()).isEmpty();
    }

    @Test
    void toPostMapReturnsEmptyListWhenMapsAreNullOrEmpty() {
        Post post = Post.builder().build();

        assertThat(PostMapper.toPostMap(post, null)).isEmpty();
        assertThat(PostMapper.toPostMap(post, List.of())).isEmpty();
    }

    @Test
    void toPostMapMapsCs2MapsToPostMapEntities() {
        Post post = Post.builder().id(UUID.randomUUID()).build();

        List<PostMap> postMaps = PostMapper.toPostMap(post, List.of(Cs2Map.MIRAGE, Cs2Map.INFERNO));

        assertThat(postMaps).hasSize(2);
        assertThat(postMaps).extracting(PostMap::getPost).containsOnly(post);
        assertThat(postMaps).extracting(PostMap::getMapName).containsExactly("MIRAGE", "INFERNO");
        assertThat(postMaps).allSatisfy(postMap -> assertThat(postMap.getCreatedAt()).isNotNull());
    }
}
