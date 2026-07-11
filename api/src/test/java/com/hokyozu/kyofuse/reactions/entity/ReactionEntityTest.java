package com.hokyozu.kyofuse.reactions.entity;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReactionEntityTest {

    @Test
    void shouldCreatePostReactionWithAllFields() {
        UUID reactionId = UUID.randomUUID();
        User user = createUser();
        Post post = createPost();
        Instant now = Instant.now();

        PostReaction reaction = PostReaction.builder()
                .id(reactionId)
                .user(user)
                .post(post)
                .reactionType(ReactionType.LIKE)
                .createdAt(now)
                .build();

        assertThat(reaction.getId()).isEqualTo(reactionId);
        assertThat(reaction.getUser()).isEqualTo(user);
        assertThat(reaction.getPost()).isEqualTo(post);
        assertThat(reaction.getReactionType()).isEqualTo(ReactionType.LIKE);
        assertThat(reaction.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateCommentReactionWithAllFields() {
        UUID reactionId = UUID.randomUUID();
        User user = createUser();
        Comment comment = createComment();
        Instant now = Instant.now();

        CommentReaction reaction = CommentReaction.builder()
                .id(reactionId)
                .user(user)
                .comment(comment)
                .reactionType(ReactionType.LOVE)
                .createdAt(now)
                .build();

        assertThat(reaction.getId()).isEqualTo(reactionId);
        assertThat(reaction.getUser()).isEqualTo(user);
        assertThat(reaction.getComment()).isEqualTo(comment);
        assertThat(reaction.getReactionType()).isEqualTo(ReactionType.LOVE);
        assertThat(reaction.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdatePostReactionType() {
        PostReaction reaction = createPostReaction();

        reaction.setReactionType(ReactionType.DISLIKE);

        assertThat(reaction.getReactionType()).isEqualTo(ReactionType.DISLIKE);
    }

    @Test
    void shouldUpdateCommentReactionType() {
        CommentReaction reaction = createCommentReaction();

        reaction.setReactionType(ReactionType.HAHA);

        assertThat(reaction.getReactionType()).isEqualTo(ReactionType.HAHA);
    }

    @Test
    void shouldPreservePostReactionUserRelationship() {
        User user = createUser();
        PostReaction reaction = createPostReactionWithUser(user);

        assertThat(reaction.getUser()).isEqualTo(user);
        assertThat(reaction.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldPreservePostReactionPostRelationship() {
        Post post = createPost();
        PostReaction reaction = createPostReactionWithPost(post);

        assertThat(reaction.getPost()).isEqualTo(post);
        assertThat(reaction.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    void shouldPreserveCommentReactionUserRelationship() {
        User user = createUser();
        CommentReaction reaction = createCommentReactionWithUser(user);

        assertThat(reaction.getUser()).isEqualTo(user);
        assertThat(reaction.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldPreserveCommentReactionCommentRelationship() {
        Comment comment = createComment();
        CommentReaction reaction = createCommentReactionWithComment(comment);

        assertThat(reaction.getComment()).isEqualTo(comment);
        assertThat(reaction.getComment().getId()).isEqualTo(comment.getId());
    }

    @Test
    void shouldHandleAllReactionTypes() {
        PostReaction postReaction = createPostReaction();

        for (ReactionType type : ReactionType.values()) {
            postReaction.setReactionType(type);
            assertThat(postReaction.getReactionType()).isEqualTo(type);
        }
    }

    @Test
    void shouldHaveDifferentCreatedAtTimes() {
        PostReaction reaction1 = createPostReaction();
        PostReaction reaction2 = createPostReaction();

        assertThat(reaction1.getCreatedAt()).isNotNull();
        assertThat(reaction2.getCreatedAt()).isNotNull();
    }

    private PostReaction createPostReaction() {
        PostReaction reaction = new PostReaction();
        reaction.setId(UUID.randomUUID());
        reaction.setUser(createUser());
        reaction.setPost(createPost());
        reaction.setReactionType(ReactionType.LIKE);
        reaction.setCreatedAt(Instant.now());
        return reaction;
    }

    private PostReaction createPostReactionWithUser(User user) {
        PostReaction reaction = new PostReaction();
        reaction.setId(UUID.randomUUID());
        reaction.setUser(user);
        reaction.setPost(createPost());
        reaction.setReactionType(ReactionType.LIKE);
        reaction.setCreatedAt(Instant.now());
        return reaction;
    }

    private PostReaction createPostReactionWithPost(Post post) {
        PostReaction reaction = new PostReaction();
        reaction.setId(UUID.randomUUID());
        reaction.setUser(createUser());
        reaction.setPost(post);
        reaction.setReactionType(ReactionType.LIKE);
        reaction.setCreatedAt(Instant.now());
        return reaction;
    }

    private CommentReaction createCommentReaction() {
        CommentReaction reaction = new CommentReaction();
        reaction.setId(UUID.randomUUID());
        reaction.setUser(createUser());
        reaction.setComment(createComment());
        reaction.setReactionType(ReactionType.LIKE);
        reaction.setCreatedAt(Instant.now());
        return reaction;
    }

    private CommentReaction createCommentReactionWithUser(User user) {
        CommentReaction reaction = new CommentReaction();
        reaction.setId(UUID.randomUUID());
        reaction.setUser(user);
        reaction.setComment(createComment());
        reaction.setReactionType(ReactionType.LIKE);
        reaction.setCreatedAt(Instant.now());
        return reaction;
    }

    private CommentReaction createCommentReactionWithComment(Comment comment) {
        CommentReaction reaction = new CommentReaction();
        reaction.setId(UUID.randomUUID());
        reaction.setUser(createUser());
        reaction.setComment(comment);
        reaction.setReactionType(ReactionType.LIKE);
        reaction.setCreatedAt(Instant.now());
        return reaction;
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
}
