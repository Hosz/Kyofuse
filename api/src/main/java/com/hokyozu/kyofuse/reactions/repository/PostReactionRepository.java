package com.hokyozu.kyofuse.reactions.repository;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.reactions.entity.PostReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, UUID> {
    Optional<PostReaction> findByPostIdAndUserId(UUID postId, UUID userId);
    List<PostReaction> findByPostIdInAndUserId(java.util.Collection<UUID> postIds, UUID userId);

    @EntityGraph(attributePaths = {"user"})
    Page<PostReaction> findByPostIdAndReactionType(UUID postId, ReactionType reactionType, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<PostReaction> findByPostIdAndReactionTypeNot(UUID postId, ReactionType reactionType, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<PostReaction> findByPostId(UUID postId, Pageable pageable);
}
