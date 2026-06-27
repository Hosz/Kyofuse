package com.hokyozu.kyofuse.reactions.service;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.finder.CommentFinder;
import com.hokyozu.kyofuse.comments.repository.CommentRepository;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.reactions.dto.request.CommentReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.CommentReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.CommentReaction;
import com.hokyozu.kyofuse.reactions.entity.PostReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.reactions.mapper.CommentReactionMapper;
import com.hokyozu.kyofuse.reactions.mapper.PostReactionMapper;
import com.hokyozu.kyofuse.reactions.repository.CommentReactionRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentReactionService {

    private final UserFinder userFinder;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;

    private final CommentReactionRepository commentReactionRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentReactionResponse upsertReaction(UUID postId, UUID commentId, @Valid CommentReactionRequest request, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        postFinder.findVisibleActivePost(postId, userId);
        Comment comment = commentFinder.findById(commentId);
        Optional<CommentReaction> commentReactionExist = commentReactionRepository.findByComment_Post_IdAndComment_IdAndUserId(postId, commentId, userId);

        if (commentReactionExist.isEmpty()) {
            CommentReaction commentReactionCreate = CommentReactionMapper.toEntity(comment, user, request);
            CommentReaction commentReactionSaved = commentReactionRepository.save(commentReactionCreate);

            if (request.reactionType() == ReactionType.LIKE) {
                comment.setLikeCount(comment.getLikeCount() + 1);
            } else {
                comment.setReactionCount(comment.getReactionCount() + 1);
            }
            commentRepository.save(comment);

            return CommentReactionMapper.toResponse(commentReactionSaved);
        } else {
            CommentReaction commentReaction = commentReactionExist.get();

            if (commentReaction.getReactionType() != ReactionType.LIKE && request.reactionType() == ReactionType.LIKE) {
                comment.setReactionCount(comment.getReactionCount() - 1);
                comment.setLikeCount(comment.getLikeCount() + 1);
            } else if (commentReaction.getReactionType() == ReactionType.LIKE && request.reactionType() != ReactionType.LIKE) {
                comment.setLikeCount(comment.getLikeCount() - 1);
                comment.setReactionCount(comment.getReactionCount() + 1);
            }
            commentRepository.save(comment);

            commentReaction.setReactionType(request.reactionType());
            commentReaction.setUpdatedAt(Instant.now());

            CommentReaction commentReactionSaved = commentReactionRepository.save(commentReaction);

            return CommentReactionMapper.toResponse(commentReactionSaved);
        }
    }

    @Transactional
    public void removeReaction(UUID userId, UUID postId, UUID commentId) {
        userFinder.findProfileByUserId(userId);
        postFinder.findVisibleActivePost(postId, userId);
        Comment comment = commentFinder.findById(commentId);
        CommentReaction commentReaction = commentReactionRepository.findByComment_Post_IdAndComment_IdAndUserId(postId, commentId, userId)
                .orElseThrow(() -> new NotFoundException("Reação não encontrada."));

        if (commentReaction.getReactionType() == ReactionType.LIKE) {
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            comment.setReactionCount(comment.getReactionCount() - 1);
        }
        commentRepository.save(comment);

        commentReactionRepository.delete(commentReaction);
    }
}
