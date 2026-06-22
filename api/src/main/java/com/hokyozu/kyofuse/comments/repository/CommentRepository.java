package com.hokyozu.kyofuse.comments.repository;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByPostIdAndStatus(UUID postId, CommentStatus commentStatus, Pageable pageable);

    Boolean existsByIdAndPostId(UUID commentId, UUID postId);
}
