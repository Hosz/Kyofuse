package com.hokyozu.kyofuse.chat.repository;

import com.hokyozu.kyofuse.chat.entity.Message;
import com.hokyozu.kyofuse.chat.entity.MessageReceipt;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, UUID> {
    Optional<MessageReceipt> findByMessageAndUser(Message message, User user);

    List<MessageReceipt> findByMessageId(UUID messageId);

    @Query("SELECT r FROM MessageReceipt r WHERE r.message.id IN :messageIds")
    List<MessageReceipt> findByMessageIdIn(@Param("messageIds") List<UUID> messageIds);

    @Query("SELECT r FROM MessageReceipt r WHERE r.message.id IN :messageIds AND r.user.id = :userId")
    List<MessageReceipt> findByMessageIdInAndUserId(@Param("messageIds") List<UUID> messageIds, @Param("userId") UUID userId);

    @Query("SELECT r FROM MessageReceipt r JOIN FETCH r.user WHERE r.message.id = :messageId")
    List<MessageReceipt> findAllWithUserByMessageId(@Param("messageId") UUID messageId);

    @Query("SELECT r FROM MessageReceipt r WHERE r.message.conversation.id = :conversationId AND r.user.id = :userId AND r.readAt IS NULL")
    List<MessageReceipt> findUnreadByConversationAndUser(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

    @Query("SELECT r.message.conversation.id, COUNT(r) FROM MessageReceipt r WHERE r.message.conversation.id IN :conversationIds AND r.user.id = :userId AND r.readAt IS NULL GROUP BY r.message.conversation.id")
    List<Object[]> countUnreadByConversationIdsAndUserId(@Param("conversationIds") List<UUID> conversationIds, @Param("userId") UUID userId);

    @Query("SELECT COUNT(r) FROM MessageReceipt r WHERE r.user.id = :userId AND r.readAt IS NULL")
    long countTotalUnreadByUserId(@Param("userId") UUID userId);
}
