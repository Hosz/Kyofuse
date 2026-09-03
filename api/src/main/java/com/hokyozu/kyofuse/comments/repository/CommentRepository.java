package com.hokyozu.kyofuse.comments.repository;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @EntityGraph(attributePaths = {"author"})
    Page<Comment> findByPostIdAndStatus(UUID postId, CommentStatus commentStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Page<Comment> findByAuthorIdAndStatus(UUID authorId, CommentStatus commentStatus, Pageable pageable);

    Boolean existsByIdAndPostId(UUID commentId, UUID postId);
}
