package com.hokyozu.kyofuse.comments.service;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.comments.repository.CommentRepository;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostFinder postFinder;

    @Mock
    private GamerProfileFinder gamerProfileFinder;

    @InjectMocks
    private CommentService commentService;

    @Test
    void postCommentCreatesActiveCommentAndIncrementsPostCommentCount() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User user = User.builder().id(userId).username("player").build();
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .build();
        Post post = Post.builder()
                .id(postId)
                .commentCount(2)
                .build();
        CreateCommentRequest request = new CreateCommentRequest("content");

        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(postFinder.findPostByIdAndStatus(postId, PostStatus.ACTIVE)).thenReturn(post);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(commentId);
            comment.setReactionCount(0);
            comment.setLikeCount(0);
            return comment;
        });

        CommentResponse response = commentService.postComment(userId, postId, request);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        verify(postRepository).save(post);

        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getPost()).isSameAs(post);
        assertThat(savedComment.getAuthor()).isSameAs(user);
        assertThat(savedComment.getContent()).isEqualTo("content");
        assertThat(savedComment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(savedComment.getCreatedAt()).isNotNull();
        assertThat(savedComment.getUpdatedAt()).isNotNull();
        assertThat(post.getCommentCount()).isEqualTo(3);
        assertThat(response.id()).isEqualTo(commentId);
        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(userId);
    }

    @Test
    void postCommentDoesNotSaveWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest("content");
        when(gamerProfileFinder.findProfileByUserId(userId))
                .thenThrow(new RuntimeException("Gamer profile not found for user ID: " + userId));

        assertThatThrownBy(() -> commentService.postComment(userId, postId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for user ID: " + userId);

        verify(postFinder, never()).findPostByIdAndStatus(any(), any());
        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void postCommentDoesNotSaveWhenPostIsNotActive() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        GamerProfile profile = GamerProfile.builder().user(user).build();
        CreateCommentRequest request = new CreateCommentRequest("content");
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(postFinder.findPostByIdAndStatus(postId, PostStatus.ACTIVE))
                .thenThrow(new BadRequestException("Post not found for ID: " + postId));

        assertThatThrownBy(() -> commentService.postComment(userId, postId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post not found for ID: " + postId);

        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void postCommentPropagatesSavedCommentCounts() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        GamerProfile profile = GamerProfile.builder().user(user).build();
        Post post = Post.builder().id(postId).commentCount(0).build();
        CreateCommentRequest request = new CreateCommentRequest("content");
        Instant now = Instant.now();
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(postFinder.findPostByIdAndStatus(postId, PostStatus.ACTIVE)).thenReturn(post);
        when(commentRepository.save(any(Comment.class))).thenReturn(Comment.builder()
                .id(commentId)
                .post(post)
                .author(user)
                .content("content")
                .status(CommentStatus.ACTIVE)
                .reactionCount(4)
                .likeCount(2)
                .createdAt(now)
                .updatedAt(now)
                .build());

        CommentResponse response = commentService.postComment(userId, postId, request);

        assertThat(response.reactionCount()).isEqualTo(4);
        assertThat(response.likeCount()).isEqualTo(2);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
