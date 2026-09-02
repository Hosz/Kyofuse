package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.dto.response.MessageMediaResponse;
import com.hokyozu.kyofuse.chat.entity.Message;
import com.hokyozu.kyofuse.chat.entity.MessageMedia;
import com.hokyozu.kyofuse.chat.mapper.MessageMediaMapper;
import com.hokyozu.kyofuse.chat.repository.MessageMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageMediaService {

    private final MessageMediaRepository messageMediaRepository;

    @Transactional
    public MessageMedia attachMediaToMessage(
            Message message,
            String fileKey,
            String url,
            String thumbnailUrl,
            String contentType,
            Long fileSizeBytes,
            Integer width,
            Integer height
    ) {
        MessageMedia messageMedia = MessageMedia.builder()
                .message(message)
                .fileKey(fileKey)
                .url(url)
                .thumbnailUrl(thumbnailUrl)
                .contentType(contentType)
                .fileSizeBytes(fileSizeBytes)
                .width(width)
                .height(height)
                .createdAt(Instant.now())
                .build();

        return messageMediaRepository.save(messageMedia);
    }

    @Transactional(readOnly = true)
    public List<MessageMediaResponse> findByMessageId(UUID messageId) {
        return messageMediaRepository.findByMessageId(messageId)
                .stream()
                .map(MessageMediaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageMedia> getEntitiesByMessageId(UUID messageId) {
        return messageMediaRepository.findByMessageId(messageId);
    }

    @Transactional(readOnly = true)
    public List<MessageMedia> getEntitiesByMessageIds(List<UUID> messageIds) {
        return messageMediaRepository.findByMessageIdIn(messageIds);
    }
}
