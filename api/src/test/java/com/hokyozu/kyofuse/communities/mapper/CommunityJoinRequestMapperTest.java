package com.hokyozu.kyofuse.communities.mapper;

import com.hokyozu.kyofuse.communities.dto.response.CommunityJoinRequestResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityJoinRequest;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityJoinRequestMapperTest {

    @Test
    void canInstantiateMapper() {
        assertThat(new CommunityJoinRequestMapper()).isNotNull();
    }

    @Test
    void toEntity_shouldBuildRequestWithCurrentTimestamp() {
        Community community = community();
        User requester = user("requester");
        Instant before = Instant.now();

        CommunityJoinRequest joinRequest = CommunityJoinRequestMapper.toEntity(requester, community);

        assertThat(joinRequest.getCommunity()).isSameAs(community);
        assertThat(joinRequest.getRequester()).isSameAs(requester);
        assertThat(joinRequest.getCreatedAt()).isBetween(before, Instant.now());
    }

    @Test
    void toResponse_shouldMapJoinRequestFields() {
        Community community = community();
        User requester = user("requester");
        Instant now = Instant.now();
        CommunityJoinRequest joinRequest = CommunityJoinRequest.builder()
                .id(UUID.randomUUID())
                .community(community)
                .requester(requester)
                .createdAt(now)
                .build();

        CommunityJoinRequestResponse response = CommunityJoinRequestMapper.toResponse(joinRequest);

        assertThat(response.id()).isEqualTo(joinRequest.getId());
        assertThat(response.communityId()).isEqualTo(community.getId());
        assertThat(response.communityName()).isEqualTo(community.getName());
        assertThat(response.communitySlug()).isEqualTo(community.getSlug());
        assertThat(response.userId()).isEqualTo(requester.getId());
        assertThat(response.username()).isEqualTo(requester.getUsername());
        assertThat(response.createdAt()).isEqualTo(now);
    }

    private Community community() {
        return Community.builder()
                .id(UUID.randomUUID())
                .owner(user("owner"))
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2")
                .visibility(CommunityVisibility.PRIVATE)
                .status(CommunityStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private User user(String username) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .build();
    }
}
