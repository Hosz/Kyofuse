package com.hokyozu.kyofuse.chat.repository;

import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByConversation(Conversation conversation, Pageable pageable);

    Optional<Message> findByIdAndConversation(UUID messageId, Conversation conversation);

    boolean existsByConversation(Conversation conversation);

    @Query("SELECT m FROM Message m WHERE m.conversation.id IN :conversationIds ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdInOrderByCreatedAtDesc(@Param("conversationIds") List<UUID> conversationIds);

    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation AND m.createdAt >= :since")
    Page<Message> findByConversationAndCreatedAtGreaterThanEqual(
            @Param("conversation") Conversation conversation,
            @Param("since") java.time.Instant since,
            Pageable pageable
    );
}
