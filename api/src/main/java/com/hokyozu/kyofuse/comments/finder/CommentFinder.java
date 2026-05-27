package com.hokyozu.kyofuse.comments.finder;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.repository.CommentRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class CommentFinder {

    private final CommentRepository commentRepository;

    public Comment findById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BadRequestException("Comment not found for ID: " + commentId));
    }
}
