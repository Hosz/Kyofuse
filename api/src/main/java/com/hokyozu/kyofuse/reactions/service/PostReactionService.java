package com.hokyozu.kyofuse.reactions.service;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.PostReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.reactions.mapper.PostReactionMapper;
import com.hokyozu.kyofuse.reactions.repository.PostReactionRepository;
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
public class PostReactionService {

    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;

    private final PostFinder postFinder;
    private final UserFinder userFinder;

    @Transactional
    public PostReactionResponse upsertReaction(UUID userId, UUID postId, @Valid PostReactionRequest request) {
        Post post = postFinder.findVisiblePostForUser(postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC);
        User user = userFinder.findProfileByUserId(userId);
        Optional<PostReaction> postReactionExist = postReactionRepository.findByPostIdAndUserId(postId, userId);

        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new BadRequestException("Post não existente");
        }

        if (postReactionExist.isEmpty()) {
            PostReaction postReactionCreate = PostReactionMapper.toEntity(post, user, request);
            PostReaction postReactionSaved = postReactionRepository.save(postReactionCreate);

            if (request.reactionType() == ReactionType.LIKE) {
                post.setLikeCount(post.getLikeCount() + 1);
            } else {
                post.setReactionCount(post.getReactionCount() + 1);
            }
            postRepository.save(post);

            return PostReactionMapper.toResponse(postReactionSaved);
        } else {
            PostReaction postReaction = postReactionExist.get();

            if (postReaction.getReactionType() != ReactionType.LIKE && request.reactionType() == ReactionType.LIKE) {
                post.setReactionCount(post.getReactionCount() - 1);
                post.setLikeCount(post.getLikeCount() + 1);
            } else if (postReaction.getReactionType() == ReactionType.LIKE && request.reactionType() != ReactionType.LIKE) {
                post.setLikeCount(post.getLikeCount() - 1);
                post.setReactionCount(post.getReactionCount() + 1);
            }
            postRepository.save(post);

            postReaction.setReactionType(request.reactionType());
            postReaction.setUpdatedAt(Instant.now());

            PostReaction postReactionSaved = postReactionRepository.save(postReaction);

            return PostReactionMapper.toResponse(postReactionSaved);
        }
    }

    @Transactional
    public void removeReaction(UUID userId, UUID postId) {
        userFinder.findProfileByUserId(userId);
        Post post = postFinder.findVisibleActivePost(postId, userId);

        PostReaction postReaction = postReactionRepository.findByPostIdAndUserId(postId, userId)
                        .orElseThrow(() -> new NotFoundException("Reação de post não encontrada."));

        if (postReaction.getReactionType() == ReactionType.LIKE) {
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            post.setReactionCount(post.getReactionCount() - 1);
        }
        postRepository.save(post);

        postReactionRepository.delete(postReaction);
    }
}
