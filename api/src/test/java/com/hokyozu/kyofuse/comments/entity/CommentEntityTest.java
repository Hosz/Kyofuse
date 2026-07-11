package com.hokyozu.kyofuse.comments.entity;

import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.posts.entity.Post;
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

class CommentEntityTest {

    @Test
    void shouldCreateCommentWithAllFields() {
        UUID commentId = UUID.randomUUID();
        User author = createUser();
        Post post = createPost();
        Instant now = Instant.now();

        Comment comment = Comment.builder()
                .id(commentId)
                .author(author)
                .post(post)
                .content("This is a great post!")
                .status(CommentStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(comment.getId()).isEqualTo(commentId);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getContent()).isEqualTo("This is a great post!");
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(comment.getCreatedAt()).isEqualTo(now);
        assertThat(comment.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateCommentContent() {
        Comment comment = createComment();
        String newContent = "Updated comment content";

        comment.setContent(newContent);

        assertThat(comment.getContent()).isEqualTo(newContent);
    }

    @Test
    void shouldUpdateCommentStatus() {
        Comment comment = createComment();

        comment.setStatus(CommentStatus.DELETED);

        assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
    }

    @Test
    void shouldPreserveAuthorRelationship() {
        User author = createUser();
        Comment comment = createCommentWithAuthor(author);

        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getAuthor().getId()).isEqualTo(author.getId());
    }

    @Test
    void shouldPreservePostRelationship() {
        Post post = createPost();
        Comment comment = createCommentWithPost(post);

        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    void shouldUpdateTimestamps() {
        Comment comment = createComment();
        Instant originalCreatedAt = comment.getCreatedAt();
        Instant newUpdatedAt = Instant.now().plusSeconds(3600);

        comment.setUpdatedAt(newUpdatedAt);

        assertThat(comment.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(comment.getUpdatedAt()).isEqualTo(newUpdatedAt);
    }

    @Test
    void shouldHandleDifferentCommentStatuses() {
        Comment comment = createComment();

        for (CommentStatus status : CommentStatus.values()) {
            comment.setStatus(status);
            assertThat(comment.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void shouldAllowLongContent() {
        Comment comment = createComment();
        String longContent = "A".repeat(1000);

        comment.setContent(longContent);

        assertThat(comment.getContent()).isEqualTo(longContent);
    }

    private Comment createComment() {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setAuthor(createUser());
        comment.setPost(createPost());
        comment.setContent("Test comment");
        comment.setStatus(CommentStatus.ACTIVE);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        return comment;
    }

    private Comment createCommentWithAuthor(User author) {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setAuthor(author);
        comment.setPost(createPost());
        comment.setContent("Test comment");
        comment.setStatus(CommentStatus.ACTIVE);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        return comment;
    }

    private Comment createCommentWithPost(Post post) {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setAuthor(createUser());
        comment.setPost(post);
        comment.setContent("Test comment");
        comment.setStatus(CommentStatus.ACTIVE);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        return comment;
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
}
