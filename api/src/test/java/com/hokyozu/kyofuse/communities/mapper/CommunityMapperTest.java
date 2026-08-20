package com.hokyozu.kyofuse.communities.mapper;

import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityMapperTest {

    @Test
    void canInstantiateMapper() {
        assertThat(new CommunityMapper()).isNotNull();
    }

    @Test
    void toEntity_shouldMapRequestToEntity() {
        User owner = createUser();
        CommunityRequest request = new CommunityRequest(
                "Kyofuse CS2",
                "kyofuse-cs2",
                "Community description",
                "https://example.com/avatar.png",
                "https://example.com/banner.png",
                CommunityVisibility.PUBLIC
        );

        Community entity = CommunityMapper.toEntity(request, owner);

        assertThat(entity).isNotNull();
        assertThat(entity.getOwner()).isSameAs(owner);
        assertThat(entity.getName()).isEqualTo("Kyofuse CS2");
        assertThat(entity.getSlug()).isEqualTo("kyofuse-cs2");
        assertThat(entity.getDescription()).isEqualTo("Community description");
        assertThat(entity.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(entity.getBannerUrl()).isEqualTo("https://example.com/banner.png");
        assertThat(entity.getVisibility()).isEqualTo(CommunityVisibility.PUBLIC);
        assertThat(entity.getStatus()).isEqualTo(CommunityStatus.ACTIVE);
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void toEntityTeamCommunity_shouldUseGivenSlugInsteadOfTeamSlug() {
        User owner = createUser();
        Team team = Team.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .name("Kyofuse Academy")
                .slug("kyofuse-academy")
                .description("Competitive team")
                .avatarUrl("https://example.com/avatar.png")
                .bannerUrl("https://example.com/banner.png")
                .build();

        // O slug resolvido pode ter sido ajustado com sufixo pelo CommunityService
        // (colisão com uma comunidade avulsa) — o mapper precisa usar exatamente o
        // valor recebido, não reler team.getSlug() por conta própria.
        Community entity = CommunityMapper.toEntityTeamCommunity(owner, team, "kyofuse-academy-2");

        assertThat(entity.getOwner()).isSameAs(owner);
        assertThat(entity.getTeam()).isSameAs(team);
        assertThat(entity.getName()).isEqualTo("Kyofuse Academy");
        assertThat(entity.getSlug()).isEqualTo("kyofuse-academy-2");
        assertThat(entity.getDescription()).isEqualTo("Competitive team");
        assertThat(entity.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(entity.getBannerUrl()).isEqualTo("https://example.com/banner.png");
        assertThat(entity.getVisibility()).isEqualTo(CommunityVisibility.PUBLIC);
        assertThat(entity.getStatus()).isEqualTo(CommunityStatus.ACTIVE);
    }

    @Test
    void toResponse_shouldMapEntityWithoutTeam() {
        Community community = createCommunity(createUser(), null);

        CommunityResponse response = CommunityMapper.toResponse(community);

        assertThat(response.id()).isEqualTo(community.getId());
        assertThat(response.communityName()).isEqualTo(community.getName());
        assertThat(response.communitySlug()).isEqualTo(community.getSlug());
        assertThat(response.ownerId()).isEqualTo(community.getOwner().getId());
        assertThat(response.ownerUsername()).isEqualTo(community.getOwner().getUsername());
        assertThat(response.teamId()).isNull();
        assertThat(response.teamName()).isNull();
        assertThat(response.visibility()).isEqualTo(CommunityVisibility.PUBLIC);
        assertThat(response.status()).isEqualTo(CommunityStatus.ACTIVE);
    }

    @Test
    void toResponse_shouldMapEntityWithTeam() {
        Team team = Team.builder()
                .id(UUID.randomUUID())
                .name("Kyofuse Academy")
                .avatarUrl("https://example.com/team.png")
                .build();
        Community community = createCommunity(createUser(), team);

        CommunityResponse response = CommunityMapper.toResponse(community);

        assertThat(response.teamId()).isEqualTo(team.getId());
        assertThat(response.teamName()).isEqualTo("Kyofuse Academy");
        assertThat(response.teamAvatarUrl()).isEqualTo("https://example.com/team.png");
    }

    @Test
    void toEdit_shouldUpdateOnlyProvidedFields() {
        Community community = createCommunity(createUser(), null);
        UpdateCommunityRequest request = new UpdateCommunityRequest(
                "Updated Name",
                null,
                null,
                null,
                null,
                null
        );

        CommunityMapper.toEdit(community, request);

        assertThat(community.getName()).isEqualTo("Updated Name");
        assertThat(community.getSlug()).isEqualTo("kyofuse-cs2");
        assertThat(community.getDescription()).isEqualTo("Community description");
        assertThat(community.getVisibility()).isEqualTo(CommunityVisibility.PUBLIC);
    }

    @Test
    void toEdit_shouldUpdateAllProvidedFieldsAndTimestamp() {
        Community community = createCommunity(createUser(), null);
        Instant previousUpdatedAt = community.getUpdatedAt();
        UpdateCommunityRequest request = new UpdateCommunityRequest(
                "Renamed Community",
                "renamed-community",
                "New description",
                "https://example.com/new-avatar.png",
                "https://example.com/new-banner.png",
                CommunityVisibility.PRIVATE
        );

        CommunityMapper.toEdit(community, request);

        assertThat(community.getName()).isEqualTo("Renamed Community");
        assertThat(community.getSlug()).isEqualTo("renamed-community");
        assertThat(community.getDescription()).isEqualTo("New description");
        assertThat(community.getAvatarUrl()).isEqualTo("https://example.com/new-avatar.png");
        assertThat(community.getBannerUrl()).isEqualTo("https://example.com/new-banner.png");
        assertThat(community.getVisibility()).isEqualTo(CommunityVisibility.PRIVATE);
        assertThat(community.getUpdatedAt()).isAfterOrEqualTo(previousUpdatedAt);
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("communityowner")
                .build();
    }

    private Community createCommunity(User owner, Team team) {
        Instant now = Instant.now();
        return Community.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .team(team)
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2")
                .description("Community description")
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
