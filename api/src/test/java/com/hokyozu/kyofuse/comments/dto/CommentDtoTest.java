package com.hokyozu.kyofuse.comments.dto;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createCommentRequestAcceptsValidPayload() {
        CreateCommentRequest request = new CreateCommentRequest("content");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void createCommentRequestRejectsBlankContent() {
        CreateCommentRequest request = new CreateCommentRequest("");

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("content");
    }

    @Test
    void createCommentRequestRejectsContentOverMaxLength() {
        CreateCommentRequest request = new CreateCommentRequest("a".repeat(2001));

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("content");
    }

    @Test
    void commentResponseExposesRecordValues() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Instant now = Instant.now();

        CommentResponse response = new CommentResponse(
                commentId,
                postId,
                "",
                authorId,
                "content",
                CommentStatus.ACTIVE,
                1,
                1,
                now,
                now
        );

        assertThat(response.id()).isEqualTo(commentId);
        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(authorId);
        assertThat(response.commentStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(response.reactionCount()).isEqualTo(1);
    }
}
