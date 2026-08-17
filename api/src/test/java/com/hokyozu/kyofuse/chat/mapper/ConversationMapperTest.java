package com.hokyozu.kyofuse.chat.mapper;

import com.hokyozu.kyofuse.chat.dto.request.ConversationRequest;
import com.hokyozu.kyofuse.chat.dto.response.ConversationResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationMapperTest {

    @Test
    void toEntityGroupBuildsGroupTypeWithoutDirectOrCommunityFields() {
        User creator = user("creator");
        ConversationRequest request = new ConversationRequest("Squad", List.of(UUID.randomUUID(), UUID.randomUUID()));

        Conversation conversation = ConversationMapper.toEntityGroup(request, creator);

        assertThat(conversation.getType()).isEqualTo(ConversationType.GROUP);
        assertThat(conversation.getName()).isEqualTo("Squad");
        assertThat(conversation.getCreatedBy()).isEqualTo(creator);
        assertThat(conversation.getCommunity()).isNull();
        assertThat(conversation.getDirectUserOne()).isNull();
        assertThat(conversation.getDirectUserTwo()).isNull();
        assertThat(conversation.getDirectMessageStatus()).isNull();
        assertThat(conversation.getCreatedAt()).isNotNull();
        assertThat(conversation.getUpdatedAt()).isNotNull();
    }

    @Test
    void toEntityDirectBuildsDirectTypeWithGivenOrderAndStatus() {
        User creator = user("creator");
        User directUserOne = user("alice");
        User directUserTwo = user("bob");

        Conversation conversation = ConversationMapper.toEntityDirect(creator, directUserOne, directUserTwo, DirectConversationStatus.PENDING);

        assertThat(conversation.getType()).isEqualTo(ConversationType.DIRECT);
        assertThat(conversation.getName()).isNull();
        assertThat(conversation.getCreatedBy()).isEqualTo(creator);
        assertThat(conversation.getCommunity()).isNull();
        assertThat(conversation.getDirectUserOne()).isEqualTo(directUserOne);
        assertThat(conversation.getDirectUserTwo()).isEqualTo(directUserTwo);
        assertThat(conversation.getDirectMessageStatus()).isEqualTo(DirectConversationStatus.PENDING);
        assertThat(conversation.getUpdatedAt()).isNotNull();
    }

    @Test
    void toEntityCommunityBuildsCommunityTypeWithoutDuplicatingName() {
        User creator = user("owner");
        Community community = Community.builder().id(UUID.randomUUID()).name("Kyofuse CS2").build();

        Conversation conversation = ConversationMapper.toEntityCommunity(community, creator);

        assertThat(conversation.getType()).isEqualTo(ConversationType.COMMUNITY);
        // name fica null de propósito: o nome de exibição vem de community.getName(),
        // não deve ser duplicado/cacheado na própria conversa.
        assertThat(conversation.getName()).isNull();
        assertThat(conversation.getCreatedBy()).isEqualTo(creator);
        assertThat(conversation.getCommunity()).isEqualTo(community);
        assertThat(conversation.getDirectUserOne()).isNull();
        assertThat(conversation.getDirectUserTwo()).isNull();
        assertThat(conversation.getDirectMessageStatus()).isNull();
    }

    @Test
    void toResponseMapsDirectConversationFields() {
        User creator = user("creator");
        User directUserOne = user("alice");
        User directUserTwo = user("bob");
        Instant now = Instant.now();
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.DIRECT)
                .createdBy(creator)
                .directUserOne(directUserOne)
                .directUserTwo(directUserTwo)
                .directMessageStatus(DirectConversationStatus.ACCEPTED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ConversationResponse response = ConversationMapper.toResponse(conversation);

        assertThat(response.id()).isEqualTo(conversation.getId());
        assertThat(response.type()).isEqualTo(ConversationType.DIRECT);
        assertThat(response.createdById()).isEqualTo(creator.getId());
        assertThat(response.createdByUsername()).isEqualTo("creator");
        assertThat(response.communityId()).isNull();
        assertThat(response.communityName()).isNull();
        assertThat(response.directUserOneId()).isEqualTo(directUserOne.getId());
        assertThat(response.directUserOneUsername()).isEqualTo("alice");
        assertThat(response.directUserTwoId()).isEqualTo(directUserTwo.getId());
        assertThat(response.directUserTwoUsername()).isEqualTo("bob");
        assertThat(response.directMessageStatus()).isEqualTo(DirectConversationStatus.ACCEPTED);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    @Test
    void toResponseMapsCommunityConversationFields() {
        User creator = user("owner");
        Community community = Community.builder().id(UUID.randomUUID()).name("Kyofuse CS2").build();
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.COMMUNITY)
                .createdBy(creator)
                .community(community)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ConversationResponse response = ConversationMapper.toResponse(conversation);

        assertThat(response.communityId()).isEqualTo(community.getId());
        assertThat(response.communityName()).isEqualTo("Kyofuse CS2");
        assertThat(response.directUserOneId()).isNull();
        assertThat(response.directUserOneUsername()).isNull();
        assertThat(response.directUserTwoId()).isNull();
        assertThat(response.directUserTwoUsername()).isNull();
        assertThat(response.directMessageStatus()).isNull();
    }

    private User user(String username) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(username + "@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
