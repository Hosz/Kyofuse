package com.hokyozu.kyofuse.reactions.repository;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.reactions.entity.PostReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostReactionRepository extends JpaRepository<PostReaction, UUID> {
    Optional<PostReaction> findByPostIdAndUserId(UUID postId, UUID userId);

    UUID post(Post post);

    Page<PostReaction> findByPostIdAndReactionType(UUID postId, ReactionType reactionType, Pageable pageable);

    Page<PostReaction> findByPostIdAndReactionTypeNot(UUID postId, ReactionType reactionType, Pageable pageable);

    Page<PostReaction> findByPostId(UUID postId, Pageable pageable);
}
