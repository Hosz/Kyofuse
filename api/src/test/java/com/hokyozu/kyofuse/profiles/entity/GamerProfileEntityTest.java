package com.hokyozu.kyofuse.profiles.entity;

import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GamerProfileEntityTest {

    @Test
    void shouldCreateGamerProfileWithAllFields() {
        UUID profileId = UUID.randomUUID();
        User user = createUser();
        Instant now = Instant.now();

        GamerProfile profile = GamerProfile.builder()
                .id(profileId)
                .user(user)
                .nickname("ProGamer")
                .bio("Competitive CS2 player")
                .avatarUrl("https://example.com/avatar.jpg")
                .country("Brazil")
                .state("São Paulo")
                .city("São Paulo")
                .mainRole(PlayerRole.RIFLER)
                .secondaryRole(PlayerRole.AWP)
                .premierRating(2500)
                .faceitLevel(8)
                .gcRank(5)
                .playstyle(Playstyle.AGGRESSIVE)
                .lookingForTeam(true)
                .lookingForDuo(false)
                .setupStatus(GamerProfileSetupStatus.COMPLETED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(profile.getId()).isEqualTo(profileId);
        assertThat(profile.getUser()).isEqualTo(user);
        assertThat(profile.getNickname()).isEqualTo("ProGamer");
        assertThat(profile.getBio()).isEqualTo("Competitive CS2 player");
        assertThat(profile.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(profile.getCountry()).isEqualTo("Brazil");
        assertThat(profile.getState()).isEqualTo("São Paulo");
        assertThat(profile.getCity()).isEqualTo("São Paulo");
        assertThat(profile.getMainRole()).isEqualTo(PlayerRole.RIFLER);
        assertThat(profile.getSecondaryRole()).isEqualTo(PlayerRole.AWP);
        assertThat(profile.getPremierRating()).isEqualTo(2500);
        assertThat(profile.getFaceitLevel()).isEqualTo(8);
        assertThat(profile.getGcRank()).isEqualTo(5);
        assertThat(profile.getPlaystyle()).isEqualTo(Playstyle.AGGRESSIVE);
        assertThat(profile.getLookingForTeam()).isTrue();
        assertThat(profile.getLookingForDuo()).isFalse();
        assertThat(profile.getSetupStatus()).isEqualTo(GamerProfileSetupStatus.COMPLETED);
        assertThat(profile.getCreatedAt()).isEqualTo(now);
        assertThat(profile.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateGamerProfileFields() {
        GamerProfile profile = createGamerProfile();
        String newNickname = "UpdatedNick";
        Integer newRating = 3000;

        profile.setNickname(newNickname);
        profile.setPremierRating(newRating);

        assertThat(profile.getNickname()).isEqualTo(newNickname);
        assertThat(profile.getPremierRating()).isEqualTo(newRating);
    }

    @Test
    void shouldUpdateSetupStatus() {
        GamerProfile profile = createGamerProfile();

        profile.setSetupStatus(GamerProfileSetupStatus.PARTIAL);

        assertThat(profile.getSetupStatus()).isEqualTo(GamerProfileSetupStatus.PARTIAL);
    }

    @Test
    void shouldAllowNullOptionalFields() {
        GamerProfile profile = new GamerProfile();
        profile.setId(UUID.randomUUID());
        profile.setUser(createUser());
        profile.setNickname("Nick");
        profile.setSetupStatus(GamerProfileSetupStatus.PENDING);
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());

        assertThat(profile.getBio()).isNull();
        assertThat(profile.getCountry()).isNull();
        assertThat(profile.getMainRole()).isNull();
        assertThat(profile.getFaceitLevel()).isNull();
    }

    @Test
    void shouldHaveDefaultLookingForValues() {
        GamerProfile profile = GamerProfile.builder()
                .user(createUser())
                .nickname("Nick")
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        assertThat(profile.getLookingForTeam()).isFalse();
        assertThat(profile.getLookingForDuo()).isFalse();
    }

    @Test
    void shouldUpdateLookingForTeam() {
        GamerProfile profile = createGamerProfile();

        profile.setLookingForTeam(true);

        assertThat(profile.getLookingForTeam()).isTrue();
    }

    @Test
    void shouldUpdateLookingForDuo() {
        GamerProfile profile = createGamerProfile();

        profile.setLookingForDuo(true);

        assertThat(profile.getLookingForDuo()).isTrue();
    }

    @Test
    void shouldPreserveUserRelationship() {
        User user = createUser();
        GamerProfile profile = createGamerProfileWithUser(user);

        assertThat(profile.getUser()).isEqualTo(user);
        assertThat(profile.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldHandleDifferentPlayerRoles() {
        GamerProfile profile = createGamerProfile();

        for (PlayerRole role : PlayerRole.values()) {
            profile.setMainRole(role);
            assertThat(profile.getMainRole()).isEqualTo(role);
        }
    }

    @Test
    void shouldHandleDifferentPlaystyles() {
        GamerProfile profile = createGamerProfile();

        for (Playstyle style : Playstyle.values()) {
            profile.setPlaystyle(style);
            assertThat(profile.getPlaystyle()).isEqualTo(style);
        }
    }

    @Test
    void shouldUpdateTimestamps() {
        GamerProfile profile = createGamerProfile();
        Instant originalCreatedAt = profile.getCreatedAt();
        Instant newUpdatedAt = Instant.now().plusSeconds(3600);

        profile.setUpdatedAt(newUpdatedAt);

        assertThat(profile.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(profile.getUpdatedAt()).isEqualTo(newUpdatedAt);
    }

    private GamerProfile createGamerProfile() {
        GamerProfile profile = new GamerProfile();
        profile.setId(UUID.randomUUID());
        profile.setUser(createUser());
        profile.setNickname("TestPlayer");
        profile.setSetupStatus(GamerProfileSetupStatus.PENDING);
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());
        profile.setLookingForTeam(false);
        profile.setLookingForDuo(false);
        return profile;
    }

    private GamerProfile createGamerProfileWithUser(User user) {
        GamerProfile profile = new GamerProfile();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);
        profile.setNickname("TestPlayer");
        profile.setSetupStatus(GamerProfileSetupStatus.PENDING);
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());
        return profile;
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
