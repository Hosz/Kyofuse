package com.hokyozu.kyofuse.comments.service;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.comments.finder.CommentFinder;
import com.hokyozu.kyofuse.comments.repository.CommentRepository;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.posts.service.MentionDetectionService;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.relationships.permission.service.comment.CommentPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.post.PostPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Mock
    private CommentFinder commentFinder;

    @Mock
    private UserFinder userFinder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserChecker userChecker;

    @Mock
    private CommentPermissionService commentPermissionService;

    @Mock
    private PostPermissionService postPermissionService;

    @Mock
    private ProfilePermissionService profilePermissionService;

    @Mock
    private MentionDetectionService mentionDetectionService;

    @InjectMocks
    private CommentService commentService;

    private static GamerProfile profileOf(User user) {
        return GamerProfile.builder().user(user).nickname("nickname").avatarUrl("avatar.png").build();
    }

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
                .content("post content")
                .commentCount(2)
                .build();
        CreateCommentRequest request = new CreateCommentRequest("content");

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(postFinder.findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        )).thenReturn(post);
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
        when(userFinder.findProfileByUserId(userId))
                .thenThrow(new RuntimeException("Gamer profile not found for user ID: " + userId));

        assertThatThrownBy(() -> commentService.postComment(userId, postId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for user ID: " + userId);

        verify(postFinder, never()).findVisiblePostForUser(any(), any(), any(), any());
        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void postCommentDoesNotSaveWhenPostIsNotActive() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        CreateCommentRequest request = new CreateCommentRequest("content");
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(postFinder.findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        ))
                .thenThrow(new BadRequestException("Post not found for ID: " + postId));

        assertThatThrownBy(() -> commentService.postComment(userId, postId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post not found for ID: " + postId);

        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void postCommentDoesNotSaveWhenPostDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        CreateCommentRequest request = new CreateCommentRequest("content");
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(postFinder.findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        ))
                .thenThrow(new BadRequestException("Post not found for ID: " + postId));

        assertThatThrownBy(() -> commentService.postComment(userId, postId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post not found for ID: " + postId);

        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void postCommentDoesNotSaveWhenPostIsDeleted() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        CreateCommentRequest request = new CreateCommentRequest("content");
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(postFinder.findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        ))
                .thenThrow(new BadRequestException("Post not found for ID: " + postId));

        assertThatThrownBy(() -> commentService.postComment(userId, postId, request))
                .isInstanceOf(BadRequestException.class);

        verify(postFinder).findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        );
        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void postCommentPropagatesSavedCommentCounts() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        GamerProfile profile = profileOf(user);
        Post post = Post.builder().id(postId).content("post content").commentCount(0).build();
        CreateCommentRequest request = new CreateCommentRequest("content");
        Instant now = Instant.now();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(postFinder.findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        )).thenReturn(post);
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

    @Test
    void getCommentReturnsMappedCommentFromFinder() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Instant now = Instant.now();
        Comment comment = comment(commentId, postId, authorId, CommentStatus.ACTIVE, now);
        User author = User.builder().id(authorId).build();
        when(userFinder.findProfileByUserId(authorId)).thenReturn(author);
        when(commentFinder.findById(commentId)).thenReturn(comment);
        when(gamerProfileFinder.findProfileByUserId(authorId)).thenReturn(profileOf(author));

        CommentResponse response = commentService.getComment(commentId, authorId);

        verify(commentFinder).findById(commentId);
        assertThat(response.id()).isEqualTo(commentId);
        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(authorId);
        assertThat(response.content()).isEqualTo("content");
        assertThat(response.commentStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(response.reactionCount()).isEqualTo(3);
        assertThat(response.likeCount()).isEqualTo(2);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    @Test
    void getCommentPropagatesBadRequestWhenCommentDoesNotExist() {
        UUID commentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(authorId)).thenReturn(User.builder().id(authorId).build());
        when(commentFinder.findById(commentId))
                .thenThrow(new BadRequestException("Comment not found for ID: " + commentId));

        assertThatThrownBy(() -> commentService.getComment(commentId, authorId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Comment not found for ID: " + commentId);
    }

    @Test
    void getCommentRejectsDeletedComment() {
        UUID commentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Comment deletedComment = comment(
                commentId,
                UUID.randomUUID(),
                authorId,
                CommentStatus.DELETED,
                Instant.now()
        );
        when(userFinder.findProfileByUserId(authorId)).thenReturn(User.builder().id(authorId).build());
        when(commentFinder.findById(commentId)).thenReturn(deletedComment);

        assertThatThrownBy(() -> commentService.getComment(commentId, authorId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Comentário não encontrado.");
    }

    @Test
    void getCommentRejectsHiddenComment() {
        UUID commentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Comment hiddenComment = comment(
                commentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                CommentStatus.HIDDEN,
                Instant.now()
        );
        when(userFinder.findProfileByUserId(authorId)).thenReturn(User.builder().id(authorId).build());
        when(commentFinder.findById(commentId)).thenReturn(hiddenComment);

        assertThatThrownBy(() -> commentService.getComment(commentId, authorId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Comentário em análise");
    }

    @Test
    void listCommentsValidatesActivePostAndReturnsOnlyActiveComments() {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Instant now = Instant.now();
        Comment firstComment = comment(UUID.randomUUID(), postId, authorId, CommentStatus.ACTIVE, now);
        Comment secondComment = comment(UUID.randomUUID(), postId, authorId, CommentStatus.ACTIVE, now);
        Post post = Post.builder().id(postId).status(PostStatus.ACTIVE).build();
        User author = User.builder().id(authorId).build();
        when(userFinder.findProfileByUserId(authorId)).thenReturn(author);
        when(postFinder.findPostByIdAndStatus(postId, PostStatus.ACTIVE)).thenReturn(post);
        when(commentRepository.findByPostIdAndStatus(postId, CommentStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(firstComment, secondComment), pageable, 2));
        when(gamerProfileFinder.findAllByUserIds(List.of(authorId))).thenReturn(List.of(profileOf(author)));

        Page<CommentResponse> response = commentService.listComments(postId, pageable, authorId);

        verify(postFinder).findPostByIdAndStatus(postId, PostStatus.ACTIVE);
        verify(postPermissionService).validateViewPost(author, post);
        verify(commentRepository).findByPostIdAndStatus(postId, CommentStatus.ACTIVE, pageable);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent())
                .extracting(CommentResponse::id)
                .containsExactly(firstComment.getId(), secondComment.getId());
        assertThat(response.getContent())
                .extracting(CommentResponse::commentStatus)
                .containsOnly(CommentStatus.ACTIVE);
    }

    @Test
    void listCommentsDoesNotQueryCommentsWhenPostIsNotActive() {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        when(userFinder.findProfileByUserId(authorId)).thenReturn(User.builder().id(authorId).build());
        when(postFinder.findPostByIdAndStatus(postId, PostStatus.ACTIVE))
                .thenThrow(new BadRequestException("Post not found for ID: " + postId));

        assertThatThrownBy(() -> commentService.listComments(postId, pageable, authorId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post not found for ID: " + postId);

        verify(commentRepository, never()).findByPostIdAndStatus(any(), any(), any());
    }

    @Test
    void listUserCommentsReturnsOwnCommentsWithoutPermissionCheck() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        User user = User.builder().id(userId).build();
        Comment comment = comment(UUID.randomUUID(), UUID.randomUUID(), userId, CommentStatus.ACTIVE, Instant.now());
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(commentRepository.findByAuthorIdAndStatus(userId, CommentStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(comment), pageable, 1));
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profileOf(user));

        Page<CommentResponse> response = commentService.listUserComments(userId, userId, pageable);

        assertThat(response.getContent()).extracting(CommentResponse::id).containsExactly(comment.getId());
        verify(profilePermissionService, never()).validateViewPosts(any(), any());
    }

    @Test
    void listUserCommentsValidatesViewPermissionForOtherViewer() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        User viewer = User.builder().id(viewerId).build();
        User target = User.builder().id(targetId).build();
        when(userFinder.findProfileByUserId(viewerId)).thenReturn(viewer);
        when(userFinder.findProfileByUserId(targetId)).thenReturn(target);
        when(commentRepository.findByAuthorIdAndStatus(targetId, CommentStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(gamerProfileFinder.findProfileByUserId(targetId)).thenReturn(profileOf(target));

        commentService.listUserComments(viewerId, targetId, pageable);

        verify(profilePermissionService).validateViewPosts(viewer, target);
    }

    @Test
    void deleteCommentLogicallyDeletesOwnCommentAndDecrementsPostCount() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant originalUpdatedAt = Instant.now().minusSeconds(60);
        Comment comment = comment(commentId, postId, userId, CommentStatus.ACTIVE, originalUpdatedAt);
        Post post = Post.builder().id(postId).commentCount(2).build();
        User user = User.builder().id(userId).build();
        when(commentFinder.findById(commentId)).thenReturn(comment);
        when(postFinder.findById(postId)).thenReturn(post);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(true);

        commentService.deleteComment(commentId, postId, userId);

        assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
        assertThat(comment.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(post.getCommentCount()).isEqualTo(1);
        verify(commentRepository).save(comment);
        verify(postRepository).save(post);
    }

    @Test
    void deleteCommentRejectsDeletionByNonAuthor() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Comment comment = comment(commentId, postId, authorId, CommentStatus.ACTIVE, Instant.now());
        Post post = Post.builder().id(postId).commentCount(1).build();
        when(commentFinder.findById(commentId)).thenReturn(comment);
        when(postFinder.findById(postId)).thenReturn(post);
        when(userFinder.findProfileByUserId(requesterId))
                .thenReturn(User.builder().id(requesterId).build());
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(true);

        assertThatThrownBy(() -> commentService.deleteComment(commentId, postId, requesterId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Apenas o autor pode remover o comentário.");

        assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(post.getCommentCount()).isEqualTo(1);
        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void deleteCommentRejectsCommentThatDoesNotBelongToPost() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Comment comment = comment(commentId, UUID.randomUUID(), userId, CommentStatus.ACTIVE, Instant.now());
        Post post = Post.builder().id(postId).commentCount(1).build();
        when(commentFinder.findById(commentId)).thenReturn(comment);
        when(postFinder.findById(postId)).thenReturn(post);
        when(userFinder.findProfileByUserId(userId)).thenReturn(User.builder().id(userId).build());
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(false);

        assertThatThrownBy(() -> commentService.deleteComment(commentId, postId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("O comentário não existe nesse post.");

        assertThat(post.getCommentCount()).isEqualTo(1);
        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void deleteCommentDoesNotDecrementPostCountTwice() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Comment comment = comment(commentId, postId, userId, CommentStatus.ACTIVE, Instant.now());
        Post post = Post.builder().id(postId).commentCount(2).build();
        User user = User.builder().id(userId).build();
        when(commentFinder.findById(commentId)).thenReturn(comment);
        when(postFinder.findById(postId)).thenReturn(post);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(true);

        commentService.deleteComment(commentId, postId, userId);

        assertThatThrownBy(() -> commentService.deleteComment(commentId, postId, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O comentário ja foi excluído");

        assertThat(post.getCommentCount()).isEqualTo(1);
        verify(commentRepository, times(1)).save(comment);
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void deleteCommentDoesNotAllowPostCountToBecomeNegative() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Comment comment = comment(commentId, postId, userId, CommentStatus.ACTIVE, Instant.now());
        Post post = Post.builder().id(postId).commentCount(0).build();
        User user = User.builder().id(userId).build();
        when(commentFinder.findById(commentId)).thenReturn(comment);
        when(postFinder.findById(postId)).thenReturn(post);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(true);

        commentService.deleteComment(commentId, postId, userId);

        assertThat(post.getCommentCount()).isZero();
        verify(postRepository).save(post);
    }

    private static Comment comment(UUID commentId, UUID postId, UUID authorId, CommentStatus status, Instant now) {
        return Comment.builder()
                .id(commentId)
                .post(Post.builder().id(postId).build())
                .author(User.builder().id(authorId).build())
                .content("content")
                .status(status)
                .reactionCount(3)
                .likeCount(2)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
