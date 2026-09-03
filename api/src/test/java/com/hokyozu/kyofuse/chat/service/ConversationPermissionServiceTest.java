package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.repository.ConversationMemberRepository;
import com.hokyozu.kyofuse.chat.repository.ConversationRepository;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationPermissionServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @Mock
    private CommunityMemberRepository communityMemberRepository;

    @InjectMocks
    private ConversationPermissionService service;

    private UUID convId;
    private UUID user1Id;
    private UUID user2Id;
    private UUID outsiderId;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        convId = UUID.randomUUID();
        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();
        outsiderId = UUID.randomUUID();

        user1 = User.builder().id(user1Id).build();
        user2 = User.builder().id(user2Id).build();
    }

    @Test
    void isParticipant_returnsFalseWhenParamsNull() {
        assertFalse(service.isParticipant(null, user1Id));
        assertFalse(service.isParticipant(convId, null));
        verifyNoInteractions(conversationRepository);
    }

    @Test
    void isParticipant_returnsFalseWhenConversationNotFound() {
        when(conversationRepository.findById(convId)).thenReturn(Optional.empty());

        assertFalse(service.isParticipant(convId, user1Id));
        verify(conversationRepository).findById(convId);
    }

    @Test
    void isParticipant_directConversation_returnsTrueForUserOne() {
        Conversation direct = Conversation.builder()
                .id(convId)
                .type(ConversationType.DIRECT)
                .directUserOne(user1)
                .directUserTwo(user2)
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(direct));

        assertTrue(service.isParticipant(convId, user1Id));
    }

    @Test
    void isParticipant_directConversation_returnsTrueForUserTwo() {
        Conversation direct = Conversation.builder()
                .id(convId)
                .type(ConversationType.DIRECT)
                .directUserOne(user1)
                .directUserTwo(user2)
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(direct));

        assertTrue(service.isParticipant(convId, user2Id));
    }

    @Test
    void isParticipant_directConversation_returnsFalseForOutsider() {
        Conversation direct = Conversation.builder()
                .id(convId)
                .type(ConversationType.DIRECT)
                .directUserOne(user1)
                .directUserTwo(user2)
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(direct));

        assertFalse(service.isParticipant(convId, outsiderId));
    }

    @Test
    void isParticipant_groupConversation_returnsTrueWhenMemberActive() {
        Conversation group = Conversation.builder()
                .id(convId)
                .type(ConversationType.GROUP)
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(group));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndStatus(
                convId, user1Id, ConversationMemberStatus.ACTIVE)).thenReturn(true);

        assertTrue(service.isParticipant(convId, user1Id));
    }

    @Test
    void isParticipant_groupConversation_returnsFalseWhenMemberNotActive() {
        Conversation group = Conversation.builder()
                .id(convId)
                .type(ConversationType.GROUP)
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(group));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndStatus(
                convId, outsiderId, ConversationMemberStatus.ACTIVE)).thenReturn(false);

        assertFalse(service.isParticipant(convId, outsiderId));
    }

    @Test
    void isParticipant_communityConversation_returnsTrueWhenCommunityMemberActive() {
        UUID communityId = UUID.randomUUID();
        Community community = Community.builder().id(communityId).build();
        Conversation communityConv = Conversation.builder()
                .id(convId)
                .type(ConversationType.COMMUNITY)
                .community(community)
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(communityConv));
        when(communityMemberRepository.existsByUserIdAndCommunityIdAndStatus(
                user1Id, communityId, CommunityMemberStatus.ACTIVE)).thenReturn(true);

        assertTrue(service.isParticipant(convId, user1Id));
    }

    @Test
    void isParticipant_communityConversation_returnsFalseWhenCommunityMemberNotActive() {
        UUID communityId = UUID.randomUUID();
        Community community = Community.builder().id(communityId).build();
        Conversation communityConv = Conversation.builder()
                .id(convId)
                .type(ConversationType.COMMUNITY)
                .community(community)
                .build();

        when(conversationRepository.findById(convId)).thenReturn(Optional.of(communityConv));
        when(communityMemberRepository.existsByUserIdAndCommunityIdAndStatus(
                outsiderId, communityId, CommunityMemberStatus.ACTIVE)).thenReturn(false);

        assertFalse(service.isParticipant(convId, outsiderId));
    }
}
