package com.hokyozu.kyofuse.comments.repository;

import com.hokyozu.kyofuse.comments.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
