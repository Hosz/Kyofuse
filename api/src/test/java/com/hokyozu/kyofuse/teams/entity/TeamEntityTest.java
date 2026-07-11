package com.hokyozu.kyofuse.teams.entity;

import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TeamEntityTest {

    @Test
    void shouldCreateTeamWithAllFields() {
        UUID teamId = UUID.randomUUID();
        User owner = createUser();
        Instant now = Instant.now();

        Team team = Team.builder()
                .id(teamId)
                .owner(owner)
                .name("Competitive Team")
                .slug("competitive-team")
                .description("A competitive esports team")
                .region("NA")
                .minPremierRating(1000)
                .maxPremierRating(3000)
                .minFaceitLevel(5)
                .maxFaceitLevel(10)
                .minGcRank(0)
                .maxGcRank(2)
                .status(TeamStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(team.getId()).isEqualTo(teamId);
        assertThat(team.getOwner()).isEqualTo(owner);
        assertThat(team.getName()).isEqualTo("Competitive Team");
        assertThat(team.getSlug()).isEqualTo("competitive-team");
        assertThat(team.getDescription()).isEqualTo("A competitive esports team");
        assertThat(team.getRegion()).isEqualTo("NA");
        assertThat(team.getMinPremierRating()).isEqualTo(1000);
        assertThat(team.getMaxPremierRating()).isEqualTo(3000);
        assertThat(team.getMinFaceitLevel()).isEqualTo(5);
        assertThat(team.getMaxFaceitLevel()).isEqualTo(10);
        assertThat(team.getMinGcRank()).isEqualTo(0);
        assertThat(team.getMaxGcRank()).isEqualTo(2);
        assertThat(team.getStatus()).isEqualTo(TeamStatus.ACTIVE);
        assertThat(team.getCreatedAt()).isEqualTo(now);
        assertThat(team.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateTeamFields() {
        Team team = createTeam();
        String newName = "Updated Team";
        String newDescription = "Updated description";

        team.setName(newName);
        team.setDescription(newDescription);
        team.setStatus(TeamStatus.INACTIVE);

        assertThat(team.getName()).isEqualTo(newName);
        assertThat(team.getDescription()).isEqualTo(newDescription);
        assertThat(team.getStatus()).isEqualTo(TeamStatus.INACTIVE);
    }

    @Test
    void shouldAllowNullOptionalFields() {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setOwner(createUser());
        team.setName("Team");
        team.setSlug("team");
        team.setStatus(TeamStatus.ACTIVE);
        team.setCreatedAt(Instant.now());
        team.setUpdatedAt(Instant.now());

        assertThat(team.getDescription()).isNull();
        assertThat(team.getRegion()).isNull();
        assertThat(team.getMinPremierRating()).isNull();
        assertThat(team.getMaxPremierRating()).isNull();
        assertThat(team.getMinFaceitLevel()).isNull();
        assertThat(team.getMaxFaceitLevel()).isNull();
    }

    @Test
    void shouldHaveDefaultValues() {
        Team team = new Team();

        assertThat(team.getId()).isNull();
        assertThat(team.getOwner()).isNull();
        assertThat(team.getName()).isNull();
        assertThat(team.getSlug()).isNull();
    }

    @Test
    void shouldPreserveOwnerRelationship() {
        User owner = createUser();
        Team team = createTeamWithOwner(owner);

        assertThat(team.getOwner()).isEqualTo(owner);
        assertThat(team.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(team.getOwner().getUsername()).isEqualTo(owner.getUsername());
    }

    @Test
    void shouldUpdateTimestamps() {
        Team team = createTeam();
        Instant originalCreatedAt = team.getCreatedAt();
        Instant newUpdatedAt = Instant.now().plusSeconds(3600);

        team.setUpdatedAt(newUpdatedAt);

        assertThat(team.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(team.getUpdatedAt()).isEqualTo(newUpdatedAt);
        assertThat(team.getUpdatedAt()).isAfter(team.getCreatedAt());
    }

    @Test
    void shouldHandleDifferentTeamStatuses() {
        Team team = createTeam();

        for (TeamStatus status : TeamStatus.values()) {
            team.setStatus(status);
            assertThat(team.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void shouldAllowSettingRatingRanges() {
        Team team = createTeam();

        team.setMinPremierRating(1000);
        team.setMaxPremierRating(3000);
        team.setMinFaceitLevel(5);
        team.setMaxFaceitLevel(10);

        assertThat(team.getMinPremierRating()).isEqualTo(1000);
        assertThat(team.getMaxPremierRating()).isEqualTo(3000);
        assertThat(team.getMinFaceitLevel()).isEqualTo(5);
        assertThat(team.getMaxFaceitLevel()).isEqualTo(10);
    }

    private Team createTeam() {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setOwner(createUser());
        team.setName("Test Team");
        team.setSlug("test-team");
        team.setStatus(TeamStatus.ACTIVE);
        team.setCreatedAt(Instant.now());
        team.setUpdatedAt(Instant.now());
        return team;
    }

    private Team createTeamWithOwner(User owner) {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setOwner(owner);
        team.setName("Test Team");
        team.setSlug("test-team");
        team.setStatus(TeamStatus.ACTIVE);
        team.setCreatedAt(Instant.now());
        team.setUpdatedAt(Instant.now());
        return team;
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
