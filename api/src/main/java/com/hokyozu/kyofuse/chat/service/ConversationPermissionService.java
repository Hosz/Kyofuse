package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.repository.ConversationMemberRepository;
import com.hokyozu.kyofuse.chat.repository.ConversationRepository;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationPermissionService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final CommunityMemberRepository communityMemberRepository;

    @Transactional(readOnly = true)
    public boolean isParticipant(UUID conversationId, UUID userId) {
        if (conversationId == null || userId == null) {
            return false;
        }

        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            return false;
        }

        Conversation conversation = conversationOpt.get();
        return switch (conversation.getType()) {
            case DIRECT -> {
                boolean isUserOne = conversation.getDirectUserOne() != null
                        && conversation.getDirectUserOne().getId().equals(userId);
                boolean isUserTwo = conversation.getDirectUserTwo() != null
                        && conversation.getDirectUserTwo().getId().equals(userId);
                yield isUserOne || isUserTwo;
            }
            case GROUP -> conversationMemberRepository.existsByConversationIdAndUserIdAndStatus(
                    conversation.getId(), userId, ConversationMemberStatus.ACTIVE);
            case COMMUNITY -> conversation.getCommunity() != null && communityMemberRepository.existsByUserIdAndCommunityIdAndStatus(
                    userId, conversation.getCommunity().getId(), CommunityMemberStatus.ACTIVE);
        };
    }
}
