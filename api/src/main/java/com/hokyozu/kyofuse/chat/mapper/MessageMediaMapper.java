package com.hokyozu.kyofuse.chat.mapper;

import com.hokyozu.kyofuse.chat.dto.response.MessageMediaResponse;
import com.hokyozu.kyofuse.chat.entity.MessageMedia;

import java.util.List;

public class MessageMediaMapper {

    public static MessageMediaResponse toResponse(MessageMedia messageMedia) {
        if (messageMedia == null) {
            return null;
        }

        return new MessageMediaResponse(
                messageMedia.getId(),
                messageMedia.getMessage() != null ? messageMedia.getMessage().getId() : null,
                messageMedia.getFileKey(),
                messageMedia.getUrl(),
                messageMedia.getThumbnailUrl(),
                messageMedia.getContentType(),
                messageMedia.getFileSizeBytes(),
                messageMedia.getWidth(),
                messageMedia.getHeight(),
                messageMedia.getCreatedAt()
        );
    }

    public static List<MessageMediaResponse> toResponseList(List<MessageMedia> mediaList) {
        if (mediaList == null || mediaList.isEmpty()) {
            return List.of();
        }
        return mediaList.stream()
                .map(MessageMediaMapper::toResponse)
                .toList();
    }
}
