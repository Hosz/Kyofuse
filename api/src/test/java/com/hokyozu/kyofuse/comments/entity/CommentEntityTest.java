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
                .content("Great post!")
                .status(CommentStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(comment.getId()).isEqualTo(commentId);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getContent()).isEqualTo("Great post!");
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(comment.getCreatedAt()).isEqualTo(now);
        assertThat(comment.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateCommentContent() {
        Comment comment = createComment();
        String newContent = "Updated comment";

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
    void shouldTrackReactionCount() {
        Comment comment = createComment();
        comment.setReactionCount(5);

        assertThat(comment.getReactionCount()).isEqualTo(5);
    }

    @Test
    void shouldTrackLikeCount() {
        Comment comment = createComment();
        comment.setLikeCount(3);

        assertThat(comment.getLikeCount()).isEqualTo(3);
    }

    @Test
    void shouldMaintainAuthorRelationship() {
        User author = createUser();
        Comment comment = Comment.builder()
                .id(UUID.randomUUID())
                .author(author)
                .post(createPost())
                .content("test")
                .status(CommentStatus.ACTIVE)
                .build();

        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getAuthor().getUsername()).isEqualTo(author.getUsername());
    }

    @Test
    void shouldMaintainPostRelationship() {
        Post post = createPost();
        Comment comment = Comment.builder()
                .id(UUID.randomUUID())
                .author(createUser())
                .post(post)
                .content("test")
                .status(CommentStatus.ACTIVE)
                .build();

        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    void shouldHandleHiddenStatus() {
        Comment comment = createComment();
        comment.setStatus(CommentStatus.HIDDEN);

        assertThat(comment.getStatus()).isEqualTo(CommentStatus.HIDDEN);
    }

    private Comment createComment() {
        return Comment.builder()
                .id(UUID.randomUUID())
                .author(createUser())
                .post(createPost())
                .content("Test comment")
                .status(CommentStatus.ACTIVE)
                .reactionCount(0)
                .likeCount(0)
                .build();
    }

    private Post createPost() {
        return Post.builder()
                .id(UUID.randomUUID())
                .author(createUser())
                .content("Test post")
                .postType(PostType.TEXT)
                .visibility(PostVisibility.PUBLIC)
                .status(PostStatus.ACTIVE)
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
