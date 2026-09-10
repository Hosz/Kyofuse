package com.hokyozu.kyofuse.comments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(

        @NotBlank(message = "Content is required")
        @Size(max = 500, message = "Content must have at most 500 characters")
        String content
) {
}
