package com.hokyozu.kyofuse.comments.service;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.comments.finder.CommentFinder;
import com.hokyozu.kyofuse.comments.mapper.CommentMapper;
import com.hokyozu.kyofuse.comments.repository.CommentRepository;
import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    private final PostFinder postFinder;
    private final CommentFinder commentFinder;
    private final UserFinder userFinder;
    private final NotificationService notificationService;
    private final UserChecker userChecker;
    private final CommentPermissionService commentPermissionService;
    private final PostPermissionService postPermissionService;
    private final ProfilePermissionService profilePermissionService;
    private final GamerProfileFinder gamerProfileFinder;

    @Transactional
    public CommentResponse postComment(UUID userId, UUID postId, CreateCommentRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Post post = postFinder.findVisiblePostForUser(postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC);

        Comment comment = CommentMapper.toEntity(user, request, post);
        Comment savedComment = commentRepository.save(comment);

        postPermissionService.validateComment(user, post);

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(post.getAuthor())
                        .actor(user)
                        .type(NotificationType.POST_COMMENT)
                        .title("Novo comentário.")
                        .message(user.getUsername() + " comentou no seu post.")
                        .targetType(NotificationTargetType.COMMENT)
                        .targetId(comment.getId())
                        .metadata(Map.of(
                                "PostPreview", post.getContent(),
                                "CommentPreview", comment.getContent()
                        ))
                        .build()
        );

        GamerProfile authorProfile = gamerProfileFinder.findProfileByUserId(user.getId());
        return CommentMapper.toResponse(savedComment, authorProfile.getAvatarUrl(), authorProfile.getNickname());
    }

    @Transactional(readOnly = true)
    public CommentResponse getComment(UUID commentId, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        Comment comment = commentFinder.findById(commentId);

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new NotFoundException("Comentário não encontrado.");
        }

        if (comment.getStatus() == CommentStatus.HIDDEN) {
            throw new UnauthorizedException("Comentário em análise");
        }

        commentPermissionService.validateViewComment(user, comment);

        GamerProfile authorProfile = gamerProfileFinder.findProfileByUserId(comment.getAuthor().getId());
        return CommentMapper.toResponse(comment, authorProfile.getAvatarUrl(), authorProfile.getNickname());
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> listComments(UUID postId, Pageable pageable, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        postFinder.findPostByIdAndStatus(postId, PostStatus.ACTIVE);

        Page<Comment> comments = commentRepository
                .findByPostIdAndStatus(postId, CommentStatus.ACTIVE, pageable);

        for (Comment comment : comments) {
            commentPermissionService.validateViewComment(user, comment);
        }

        return comments.map(comment -> {
            GamerProfile authorProfile = gamerProfileFinder.findProfileByUserId(comment.getAuthor().getId());
            return CommentMapper.toResponse(comment, authorProfile.getAvatarUrl(), authorProfile.getNickname());
        });
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> listUserComments(UUID viewerId, UUID targetUserId, Pageable pageable) {
        User viewer = userFinder.findProfileByUserId(viewerId);
        User target = userFinder.findProfileByUserId(targetUserId);
        userChecker.checkActive(viewer);
        userChecker.checkActive(target);

        if (!viewerId.equals(targetUserId)) {
            profilePermissionService.validateViewPosts(viewer, target);
        }

        Page<Comment> comments = commentRepository.findByAuthorIdAndStatus(targetUserId, CommentStatus.ACTIVE, pageable);

        GamerProfile targetProfile = gamerProfileFinder.findProfileByUserId(targetUserId);
        return comments.map(comment -> CommentMapper.toResponse(comment, targetProfile.getAvatarUrl(), targetProfile.getNickname()));
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID postId, UUID user) {

        Comment comment = commentFinder.findById(commentId);
        Post post = postFinder.findById(postId);
        User userProfile = userFinder.findProfileByUserId(user);

        if (!commentRepository.existsByIdAndPostId(commentId, postId)) {
            throw new NotFoundException("O comentário não existe nesse post.");
        }

        if (!comment.getAuthor().getId().equals(userProfile.getId())) {
            throw new ForbiddenException("Apenas o autor pode remover o comentário.");
        }

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new BadRequestException("O comentário ja foi excluído");
        }

        comment.setStatus(CommentStatus.DELETED);
        comment.setUpdatedAt(Instant.now());
        commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() - 1);

        if (post.getCommentCount() < 0) {
            post.setCommentCount(0);
        }

        postRepository.save(post);
    }
}
