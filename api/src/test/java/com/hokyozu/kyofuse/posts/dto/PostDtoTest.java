package com.hokyozu.kyofuse.posts.dto;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createPostRequestAcceptsValidPayload() {
        CreatePostRequest request = new CreatePostRequest("content", PostType.TEXT, PostVisibility.PUBLIC);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void createPostRequestRejectsBlankContentAndNullEnums() {
        CreatePostRequest request = new CreatePostRequest("", null, null);

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("content", "postType", "visibility");
    }

    @Test
    void createPostRequestRejectsContentOverMaxLength() {
        CreatePostRequest request = new CreatePostRequest("a".repeat(2001), PostType.TEXT, PostVisibility.PUBLIC);

        Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("content");
    }

    @Test
    void postResponseExposesRecordValues() {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Instant now = Instant.now();

        PostResponse response = new PostResponse(
                postId,
                authorId,
                "content",
                PostType.TEXT,
                PostVisibility.PUBLIC,
                PostStatus.ACTIVE,
                1,
                1,
                2,
                now,
                now
        );

        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(authorId);
        assertThat(response.reactionCount()).isEqualTo(1);
        assertThat(response.commentCount()).isEqualTo(2);
    }
}
