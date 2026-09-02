package com.hokyozu.kyofuse.chat.repository;

import com.hokyozu.kyofuse.chat.entity.MessageMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageMediaRepository extends JpaRepository<MessageMedia, UUID> {
    List<MessageMedia> findByMessageId(UUID messageId);

    List<MessageMedia> findByMessageIdIn(List<UUID> messageIds);
}
