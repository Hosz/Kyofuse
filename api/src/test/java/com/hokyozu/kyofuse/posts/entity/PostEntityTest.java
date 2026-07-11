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
                .title("My Gaming Post")
                .content("Great gameplay tips!")
                .type(PostType.TIP)
                .visibility(PostVisibility.PUBLIC)
                .status(PostStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(post.getId()).isEqualTo(postId);
        assertThat(post.getAuthor()).isEqualTo(author);
        assertThat(post.getTitle()).isEqualTo("My Gaming Post");
        assertThat(post.getContent()).isEqualTo("Great gameplay tips!");
        assertThat(post.getType()).isEqualTo(PostType.TIP);
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

        post.setVisibility(PostVisibility.PRIVATE);

        assertThat(post.getVisibility()).isEqualTo(PostVisibility.PRIVATE);
    }

    @Test
    void shouldUpdatePostStatus() {
        Post post = createPost();

        post.setStatus(PostStatus.HIDDEN);

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    void shouldPreserveAuthorRelationship() {
        User author = createUser();
        Post post = createPostWithAuthor(author);

        assertThat(post.getAuthor()).isEqualTo(author);
        assertThat(post.getAuthor().getId()).isEqualTo(author.getId());
    }

    @Test
    void shouldHandleDifferentPostTypes() {
        Post post = createPost();

        for (PostType type : PostType.values()) {
            post.setType(type);
            assertThat(post.getType()).isEqualTo(type);
        }
    }

    @Test
    void shouldHandleDifferentVisibilities() {
        Post post = createPost();

        for (PostVisibility visibility : PostVisibility.values()) {
            post.setVisibility(visibility);
            assertThat(post.getVisibility()).isEqualTo(visibility);
        }
    }

    @Test
    void shouldHandleDifferentPostStatuses() {
        Post post = createPost();

        for (PostStatus status : PostStatus.values()) {
            post.setStatus(status);
            assertThat(post.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void shouldUpdateTimestamps() {
        Post post = createPost();
        Instant originalCreatedAt = post.getCreatedAt();
        Instant newUpdatedAt = Instant.now().plusSeconds(3600);

        post.setUpdatedAt(newUpdatedAt);

        assertThat(post.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(post.getUpdatedAt()).isEqualTo(newUpdatedAt);
    }

    private Post createPost() {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setAuthor(createUser());
        post.setTitle("Test Post");
        post.setContent("Test content");
        post.setType(PostType.GENERAL);
        post.setVisibility(PostVisibility.PUBLIC);
        post.setStatus(PostStatus.ACTIVE);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        return post;
    }

    private Post createPostWithAuthor(User author) {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setAuthor(author);
        post.setTitle("Test Post");
        post.setContent("Test content");
        post.setType(PostType.GENERAL);
        post.setVisibility(PostVisibility.PUBLIC);
        post.setStatus(PostStatus.ACTIVE);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        return post;
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
