package com.hokyozu.kyofuse.chat.repository;

import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @EntityGraph(attributePaths = {"sender"})
    Page<Message> findByConversation(Conversation conversation, Pageable pageable);

    Optional<Message> findByIdAndConversation(UUID messageId, Conversation conversation);

    boolean existsByConversation(Conversation conversation);

    @EntityGraph(attributePaths = {"sender", "conversation"})
    @Query("""
        SELECT m FROM Message m
        WHERE m.conversation.id IN :conversationIds
          AND m.createdAt = (
              SELECT MAX(m2.createdAt) FROM Message m2 WHERE m2.conversation.id = m.conversation.id
          )
    """)
    List<Message> findLatestMessagesByConversationIds(@Param("conversationIds") List<UUID> conversationIds);

    @Query("SELECT m FROM Message m WHERE m.conversation.id IN :conversationIds ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdInOrderByCreatedAtDesc(@Param("conversationIds") List<UUID> conversationIds);

    @EntityGraph(attributePaths = {"sender"})
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation AND m.createdAt >= :since")
    Page<Message> findByConversationAndCreatedAtGreaterThanEqual(
            @Param("conversation") Conversation conversation,
            @Param("since") Instant since,
            Pageable pageable
    );
}
