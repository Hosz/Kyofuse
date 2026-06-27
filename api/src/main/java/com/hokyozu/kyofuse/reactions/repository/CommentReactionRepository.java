package com.hokyozu.kyofuse.reactions.repository;

import com.hokyozu.kyofuse.reactions.entity.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, UUID> {
    Optional<CommentReaction> findByComment_Post_IdAndComment_IdAndUserId(UUID postId, UUID commentId, UUID userId);
}
