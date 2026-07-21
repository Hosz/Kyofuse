package com.hokyozu.kyofuse.comments.mapper;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void defaultConstructorCanBeCreated() {
        assertThat(new CommentMapper()).isNotNull();
    }

    @Test
    void toEntityAndToResponseMapCommentFields() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        GamerProfile profile = GamerProfile.builder().user(user).build();
        Post post = Post.builder().id(postId).build();
        CreateCommentRequest request = new CreateCommentRequest("content");

        Comment comment = CommentMapper.toEntity(user, request, post);
        comment.setId(commentId);
        comment.setReactionCount(3);
        comment.setLikeCount(2);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        CommentResponse response = CommentMapper.toResponse(comment);

        assertThat(comment.getPost()).isSameAs(post);
        assertThat(comment.getAuthor()).isSameAs(user);
        assertThat(comment.getContent()).isEqualTo("content");
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(comment.getUpdatedAt()).isNotNull();
        assertThat(response.id()).isEqualTo(commentId);
        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(userId);
        assertThat(response.reactionCount()).isEqualTo(3);
        assertThat(response.likeCount()).isEqualTo(2);
    }
}
