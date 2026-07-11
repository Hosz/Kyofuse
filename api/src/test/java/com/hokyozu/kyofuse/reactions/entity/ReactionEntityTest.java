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
                .updatedAt(now)
                .build();

        assertThat(reaction.getId()).isEqualTo(reactionId);
        assertThat(reaction.getUser()).isEqualTo(user);
        assertThat(reaction.getPost()).isEqualTo(post);
        assertThat(reaction.getReactionType()).isEqualTo(ReactionType.LIKE);
        assertThat(reaction.getCreatedAt()).isEqualTo(now);
        assertThat(reaction.getUpdatedAt()).isEqualTo(now);
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
                .reactionType(ReactionType.LIKE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(reaction.getId()).isEqualTo(reactionId);
        assertThat(reaction.getUser()).isEqualTo(user);
        assertThat(reaction.getComment()).isEqualTo(comment);
        assertThat(reaction.getReactionType()).isEqualTo(ReactionType.LIKE);
        assertThat(reaction.getCreatedAt()).isEqualTo(now);
        assertThat(reaction.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdatePostReactionType() {
        PostReaction reaction = createPostReaction();
        reaction.setReactionType(ReactionType.FIRE);

        assertThat(reaction.getReactionType()).isEqualTo(ReactionType.FIRE);
    }

    @Test
    void shouldUpdateCommentReactionType() {
        CommentReaction reaction = createCommentReaction();
        reaction.setReactionType(ReactionType.FIRE);

        assertThat(reaction.getReactionType()).isEqualTo(ReactionType.FIRE);
    }

    @Test
    void shouldMaintainPostReactionUserRelationship() {
        User user = createUser();
        Post post = createPost();
        PostReaction reaction = PostReaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .post(post)
                .reactionType(ReactionType.LIKE)
                .build();

        assertThat(reaction.getUser()).isEqualTo(user);
        assertThat(reaction.getUser().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void shouldMaintainPostReactionPostRelationship() {
        Post post = createPost();
        PostReaction reaction = PostReaction.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .post(post)
                .reactionType(ReactionType.LIKE)
                .build();

        assertThat(reaction.getPost()).isEqualTo(post);
        assertThat(reaction.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    void shouldMaintainCommentReactionUserRelationship() {
        User user = createUser();
        Comment comment = createComment();
        CommentReaction reaction = CommentReaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .comment(comment)
                .reactionType(ReactionType.LIKE)
                .build();

        assertThat(reaction.getUser()).isEqualTo(user);
    }

    @Test
    void shouldMaintainCommentReactionCommentRelationship() {
        Comment comment = createComment();
        CommentReaction reaction = CommentReaction.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .comment(comment)
                .reactionType(ReactionType.LIKE)
                .build();

        assertThat(reaction.getComment()).isEqualTo(comment);
    }

    @Test
    void shouldHandleDifferentPostReactionTypes() {
        PostReaction likeReaction = createPostReaction();
        likeReaction.setReactionType(ReactionType.LIKE);

        PostReaction fireReaction = createPostReaction();
        fireReaction.setReactionType(ReactionType.FIRE);

        assertThat(likeReaction.getReactionType()).isEqualTo(ReactionType.LIKE);
        assertThat(fireReaction.getReactionType()).isEqualTo(ReactionType.FIRE);
    }

    @Test
    void shouldHandleDifferentCommentReactionTypes() {
        CommentReaction likeReaction = createCommentReaction();
        likeReaction.setReactionType(ReactionType.LIKE);

        CommentReaction clutchReaction = createCommentReaction();
        clutchReaction.setReactionType(ReactionType.CLUTCH);

        assertThat(likeReaction.getReactionType()).isEqualTo(ReactionType.LIKE);
        assertThat(clutchReaction.getReactionType()).isEqualTo(ReactionType.CLUTCH);
    }

    @Test
    void shouldHandleAllReactionTypes() {
        PostReaction reaction1 = createPostReaction();
        reaction1.setReactionType(ReactionType.LIKE);
        assertThat(reaction1.getReactionType()).isEqualTo(ReactionType.LIKE);

        PostReaction reaction2 = createPostReaction();
        reaction2.setReactionType(ReactionType.FIRE);
        assertThat(reaction2.getReactionType()).isEqualTo(ReactionType.FIRE);

        PostReaction reaction3 = createPostReaction();
        reaction3.setReactionType(ReactionType.CLUTCH);
        assertThat(reaction3.getReactionType()).isEqualTo(ReactionType.CLUTCH);

        PostReaction reaction4 = createPostReaction();
        reaction4.setReactionType(ReactionType.NICE_SHOT);
        assertThat(reaction4.getReactionType()).isEqualTo(ReactionType.NICE_SHOT);

        PostReaction reaction5 = createPostReaction();
        reaction5.setReactionType(ReactionType.LOL);
        assertThat(reaction5.getReactionType()).isEqualTo(ReactionType.LOL);
    }

    private PostReaction createPostReaction() {
        return PostReaction.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .post(createPost())
                .reactionType(ReactionType.LIKE)
                .build();
    }

    private CommentReaction createCommentReaction() {
        return CommentReaction.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .comment(createComment())
                .reactionType(ReactionType.LIKE)
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

    private Comment createComment() {
        return Comment.builder()
                .id(UUID.randomUUID())
                .post(createPost())
                .author(createUser())
                .content("Test comment")
                .status(CommentStatus.ACTIVE)
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
