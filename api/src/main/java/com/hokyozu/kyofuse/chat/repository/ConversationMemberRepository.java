package com.hokyozu.kyofuse.chat.repository;

import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.ConversationMember;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {
    // conversation_members nunca é removida fisicamente: retorna o vínculo independente
    // do status (ACTIVE, LEFT, REMOVED, KICKED); quem chama decide o que fazer com ele.
    Optional<ConversationMember> findByConversationAndUser(Conversation conversation, User user);

    Page<ConversationMember> findByConversation(Conversation conversation, Pageable pageable);

    Page<ConversationMember> findByUserAndStatus(User user, ConversationMemberStatus status, Pageable pageable);

    // Sem paginação de propósito: usado pra notificar todos os membros ativos de um
    // grupo quando alguém manda uma mensagem, não pra exibir uma listagem.
    List<ConversationMember> findByConversationAndStatus(Conversation conversation, ConversationMemberStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT cm FROM ConversationMember cm WHERE cm.conversation.id IN :conversationIds AND cm.user.id = :userId")
    List<ConversationMember> findByConversationIdInAndUserId(@org.springframework.data.repository.query.Param("conversationIds") List<UUID> conversationIds, @org.springframework.data.repository.query.Param("userId") UUID userId);
}
