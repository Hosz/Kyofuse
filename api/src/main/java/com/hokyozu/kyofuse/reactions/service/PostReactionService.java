package com.hokyozu.kyofuse.reactions.service;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.PostReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.reactions.mapper.PostReactionMapper;
import com.hokyozu.kyofuse.reactions.repository.PostReactionRepository;
import com.hokyozu.kyofuse.relationships.permission.service.post.PostPermissionService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final PostPermissionService postPermissionService;
    private final GamerProfileFinder gamerProfileFinder;
    private final UserChecker userChecker;

    @Transactional
    public PostReactionResponse upsertReaction(UUID userId, UUID postId, @Valid PostReactionRequest request) {
        Post post = postFinder.findVisiblePostForUser(postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC);
        User user = userFinder.findProfileByUserId(userId);
        Optional<PostReaction> postReactionExist = postReactionRepository.findByPostIdAndUserId(postId, userId);

        postPermissionService.validateReact(user, post);

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
            GamerProfile profile = gamerProfileFinder.findProfileByUserId(user.getId());

            return PostReactionMapper.toResponse(postReactionSaved, profile);
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
            GamerProfile profile = gamerProfileFinder.findProfileByUserId(user.getId());

            return PostReactionMapper.toResponse(postReactionSaved, profile);
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

    @Transactional(readOnly = true)
    /**
     * Todas as reações do post, curtidas incluídas: a listagem no front é uma só, e o
     * tipo aparece no ícone ao lado de cada pessoa em vez de virar aba separada.
     */
    public Page<PostReactionResponse> getReactions(UUID userId, UUID postId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        postFinder.findVisibleActivePost(postId, userId);

        userChecker.checkActive(user);

        Page<PostReaction> postReactionExist = postReactionRepository.findByPostId(postId, pageable);

        return postReactionExist.map(postReaction -> {
            GamerProfile profile = gamerProfileFinder.findProfileByUserId(postReaction.getUser().getId());
            return PostReactionMapper.toResponse(postReaction, profile);
        });
    }

    @Transactional(readOnly = true)
    public Page<PostReactionResponse> getLikes(UUID userId, UUID postId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        postFinder.findVisibleActivePost(postId, userId);

        userChecker.checkActive(user);

        Page<PostReaction> postReactionExist = postReactionRepository.findByPostIdAndReactionType(postId, ReactionType.LIKE, pageable);

        return postReactionExist.map(postReaction -> {
            GamerProfile profile = gamerProfileFinder.findProfileByUserId(postReaction.getUser().getId());
            return PostReactionMapper.toResponse(postReaction, profile);
        });

    }
}
