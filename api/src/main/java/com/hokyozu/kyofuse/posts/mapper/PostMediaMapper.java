package com.hokyozu.kyofuse.posts.mapper;

import com.hokyozu.kyofuse.posts.dto.response.PostMediaResponse;
import com.hokyozu.kyofuse.posts.entity.PostMedia;

import java.util.List;

public class PostMediaMapper {

    public static PostMediaResponse toResponse(PostMedia postMedia) {
        if (postMedia == null) {
            return null;
        }

        return new PostMediaResponse(
                postMedia.getId(),
                postMedia.getPost() != null ? postMedia.getPost().getId() : null,
                postMedia.getFileKey(),
                postMedia.getUrl(),
                postMedia.getThumbnailUrl(),
                postMedia.getContentType(),
                postMedia.getFileSizeBytes(),
                postMedia.getWidth(),
                postMedia.getHeight(),
                postMedia.getDisplayOrder(),
                postMedia.getCreatedAt()
        );
    }

    public static List<PostMediaResponse> toResponseList(List<PostMedia> mediaList) {
        if (mediaList == null || mediaList.isEmpty()) {
            return List.of();
        }
        return mediaList.stream()
                .map(PostMediaMapper::toResponse)
                .toList();
    }
}
