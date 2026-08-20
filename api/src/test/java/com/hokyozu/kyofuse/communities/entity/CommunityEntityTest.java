package com.hokyozu.kyofuse.communities.entity;

import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityEntityTest {

    @Test
    void shouldCreateCommunityWithAllFields() {
        UUID communityId = UUID.randomUUID();
        User owner = createUser();
        Team team = Team.builder().id(UUID.randomUUID()).name("Kyofuse Academy").build();
        Instant now = Instant.now();

        Community community = Community.builder()
                .id(communityId)
                .owner(owner)
                .team(team)
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2")
                .description("Community description")
                .avatarUrl("https://example.com/avatar.png")
                .bannerUrl("https://example.com/banner.png")
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(community.getId()).isEqualTo(communityId);
        assertThat(community.getOwner()).isEqualTo(owner);
        assertThat(community.getTeam()).isEqualTo(team);
        assertThat(community.getName()).isEqualTo("Kyofuse CS2");
        assertThat(community.getSlug()).isEqualTo("kyofuse-cs2");
        assertThat(community.getDescription()).isEqualTo("Community description");
        assertThat(community.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(community.getBannerUrl()).isEqualTo("https://example.com/banner.png");
        assertThat(community.getVisibility()).isEqualTo(CommunityVisibility.PUBLIC);
        assertThat(community.getStatus()).isEqualTo(CommunityStatus.ACTIVE);
        assertThat(community.getCreatedAt()).isEqualTo(now);
        assertThat(community.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldAllowNullOptionalFields() {
        Community community = new Community();
        community.setId(UUID.randomUUID());
        community.setOwner(createUser());
        community.setName("Kyofuse CS2");
        community.setSlug("kyofuse-cs2");
        community.setVisibility(CommunityVisibility.PUBLIC);
        community.setStatus(CommunityStatus.ACTIVE);
        community.setCreatedAt(Instant.now());
        community.setUpdatedAt(Instant.now());

        assertThat(community.getTeam()).isNull();
        assertThat(community.getDescription()).isNull();
        assertThat(community.getAvatarUrl()).isNull();
        assertThat(community.getBannerUrl()).isNull();
    }

    @Test
    void shouldUpdateMutableFields() {
        Community community = createCommunity();

        community.setName("Renamed");
        community.setDescription("New description");
        community.setStatus(CommunityStatus.ARCHIVED);
        community.setVisibility(CommunityVisibility.PRIVATE);

        assertThat(community.getName()).isEqualTo("Renamed");
        assertThat(community.getDescription()).isEqualTo("New description");
        assertThat(community.getStatus()).isEqualTo(CommunityStatus.ARCHIVED);
        assertThat(community.getVisibility()).isEqualTo(CommunityVisibility.PRIVATE);
    }

    @Test
    void shouldHandleAllStatusAndVisibilityValues() {
        Community community = createCommunity();

        for (CommunityStatus status : CommunityStatus.values()) {
            community.setStatus(status);
            assertThat(community.getStatus()).isEqualTo(status);
        }

        for (CommunityVisibility visibility : CommunityVisibility.values()) {
            community.setVisibility(visibility);
            assertThat(community.getVisibility()).isEqualTo(visibility);
        }
    }

    @Test
    void shouldPreserveOwnerRelationship() {
        User owner = createUser();
        Community community = createCommunity();
        community.setOwner(owner);

        assertThat(community.getOwner()).isEqualTo(owner);
        assertThat(community.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(community.getOwner().getUsername()).isEqualTo(owner.getUsername());
    }

    private Community createCommunity() {
        Instant now = Instant.now();
        return Community.builder()
                .id(UUID.randomUUID())
                .owner(createUser())
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2")
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("communityowner")
                .email("owner@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
