package com.hokyozu.kyofuse.posts.dto.request;

import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @NotBlank(message = "Content is required")
        @Size(max = 2000, message = "Content must have at most 2000 characters")
        String content,

        @NotNull(message = "Post type is required")
        PostType postType,

        @NotNull(message = "Post visibility is required")
        PostVisibility visibility
) {
}
