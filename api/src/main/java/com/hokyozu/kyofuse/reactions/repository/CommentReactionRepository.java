package com.hokyozu.kyofuse.reactions.repository;

import com.hokyozu.kyofuse.reactions.entity.CommentReaction;
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
public interface CommentReactionRepository extends JpaRepository<CommentReaction, UUID> {
    Optional<CommentReaction> findByComment_Post_IdAndComment_IdAndUserId(UUID postId, UUID commentId, UUID userId);

    @EntityGraph(attributePaths = {"user"})
    Page<CommentReaction> findByComment_Post_IdAndComment_IdAndReactionType(UUID commentPostId, UUID commentId, ReactionType reactionType, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<CommentReaction> findByComment_Post_IdAndComment_IdAndReactionTypeNot(UUID commentPostId, UUID commentId, ReactionType reactionType, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<CommentReaction> findByComment_Post_IdAndComment_Id(UUID postId, UUID commentId, Pageable pageable);
}
