package com.hokyozu.kyofuse.posts.dto.request;

import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(

        @Size(max = 2000, message = "Content must have at most 2000 characters")
        String content,

        @NotNull(message = "Post type is required")
        PostType postType,

        @NotNull(message = "Post visibility is required")
        PostVisibility visibility,

        List<@NotNull Cs2Map> maps,

        List<@Valid PostMediaItemRequest> media
) {
    public CreatePostRequest(String content, PostType postType, PostVisibility visibility, List<Cs2Map> maps) {
        this(content, postType, visibility, maps, List.of());
    }
}
