package com.hokyozu.kyofuse.chat.repository;

import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByConversation(Conversation conversation, Pageable pageable);

    Optional<Message> findByIdAndConversation(UUID messageId, Conversation conversation);

    boolean existsByConversation(Conversation conversation);
}
