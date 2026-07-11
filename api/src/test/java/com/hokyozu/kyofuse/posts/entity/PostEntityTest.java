package com.hokyozu.kyofuse.posts.entity;

import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostEntityTest {

    @Test
    void shouldCreatePostWithAllFields() {
        UUID postId = UUID.randomUUID();
        User author = createUser();
        Instant now = Instant.now();

        Post post = Post.builder()
                .id(postId)
                .author(author)
                .content("Great gameplay tips!")
                .postType(PostType.LINEUP_TIP)
                .visibility(PostVisibility.PUBLIC)
                .status(PostStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(post.getId()).isEqualTo(postId);
        assertThat(post.getAuthor()).isEqualTo(author);
        assertThat(post.getContent()).isEqualTo("Great gameplay tips!");
        assertThat(post.getPostType()).isEqualTo(PostType.LINEUP_TIP);
        assertThat(post.getVisibility()).isEqualTo(PostVisibility.PUBLIC);
        assertThat(post.getStatus()).isEqualTo(PostStatus.ACTIVE);
        assertThat(post.getCreatedAt()).isEqualTo(now);
        assertThat(post.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdatePostContent() {
        Post post = createPost();
        String newContent = "Updated content";

        post.setContent(newContent);

        assertThat(post.getContent()).isEqualTo(newContent);
    }

    @Test
    void shouldUpdatePostVisibility() {
        Post post = createPost();
        PostVisibility newVisibility = PostVisibility.PRIVATE;

        post.setVisibility(newVisibility);

        assertThat(post.getVisibility()).isEqualTo(newVisibility);
    }

    @Test
    void shouldUpdatePostStatus() {
        Post post = createPost();
        post.setStatus(PostStatus.DELETED);

        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    void shouldTrackReactionCount() {
        Post post = createPost();
        post.setReactionCount(5);

        assertThat(post.getReactionCount()).isEqualTo(5);
    }

    @Test
    void shouldTrackCommentCount() {
        Post post = createPost();
        post.setCommentCount(3);

        assertThat(post.getCommentCount()).isEqualTo(3);
    }

    @Test
    void shouldTrackLikeCount() {
        Post post = createPost();
        post.setLikeCount(10);

        assertThat(post.getLikeCount()).isEqualTo(10);
    }

    @Test
    void shouldMaintainAuthorRelationship() {
        User author = createUser();
        Post post = Post.builder()
                .id(UUID.randomUUID())
                .author(author)
                .content("test")
                .postType(PostType.TEXT)
                .visibility(PostVisibility.PUBLIC)
                .status(PostStatus.ACTIVE)
                .build();

        assertThat(post.getAuthor()).isEqualTo(author);
        assertThat(post.getAuthor().getUsername()).isEqualTo(author.getUsername());
    }

    private Post createPost() {
        return Post.builder()
                .id(UUID.randomUUID())
                .author(createUser())
                .content("Test post content")
                .postType(PostType.TEXT)
                .visibility(PostVisibility.PUBLIC)
                .status(PostStatus.ACTIVE)
                .reactionCount(0)
                .likeCount(0)
                .commentCount(0)
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("postauthor")
                .email("author@example.com")
                .firstName("Post")
                .lastName("Author")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
