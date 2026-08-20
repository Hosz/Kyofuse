package com.hokyozu.kyofuse.profiles.entity;

import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GamerProfileEntityTest {

    @Test
    void shouldCreateProfileWithAllFields() {
        UUID profileId = UUID.randomUUID();
        User user = createUser();
        Instant now = Instant.now();

        GamerProfile profile = GamerProfile.builder()
                .id(profileId)
                .user(user)
                .nickname("ProPlayer")
                .bio("Competitive CS2 player")
                .avatarUrl("https://example.com/avatar.png")
                .bannerUrl("https://example.com/banner.png")
                .country("USA")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(2500)
                .faceitLevel(8)
                .gcRank(5)
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.COMPLETED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(profile.getId()).isEqualTo(profileId);
        assertThat(profile.getUser()).isEqualTo(user);
        assertThat(profile.getNickname()).isEqualTo("ProPlayer");
        assertThat(profile.getBio()).isEqualTo("Competitive CS2 player");
        assertThat(profile.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(profile.getBannerUrl()).isEqualTo("https://example.com/banner.png");
        assertThat(profile.getCountry()).isEqualTo("USA");
        assertThat(profile.getMainRole()).isEqualTo(PlayerRole.AWPER);
        assertThat(profile.getSecondaryRole()).isEqualTo(PlayerRole.RIFLER);
        assertThat(profile.getPremierRating()).isEqualTo(2500);
        assertThat(profile.getFaceitLevel()).isEqualTo(8);
        assertThat(profile.getGcRank()).isEqualTo(5);
        assertThat(profile.getPlaystyle()).isEqualTo(Playstyle.COMPETITIVE);
        assertThat(profile.getSetupStatus()).isEqualTo(GamerProfileSetupStatus.COMPLETED);
        assertThat(profile.getCreatedAt()).isEqualTo(now);
        assertThat(profile.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateProfileNickname() {
        GamerProfile profile = createProfile();
        profile.setNickname("NewNickname");

        assertThat(profile.getNickname()).isEqualTo("NewNickname");
    }

    @Test
    void shouldUpdateProfileBio() {
        GamerProfile profile = createProfile();
        profile.setBio("Updated bio");

        assertThat(profile.getBio()).isEqualTo("Updated bio");
    }

    @Test
    void shouldUpdateProfileRatings() {
        GamerProfile profile = createProfile();
        profile.setPremierRating(3000);
        profile.setFaceitLevel(9);
        profile.setGcRank(10);

        assertThat(profile.getPremierRating()).isEqualTo(3000);
        assertThat(profile.getFaceitLevel()).isEqualTo(9);
        assertThat(profile.getGcRank()).isEqualTo(10);
    }

    @Test
    void shouldUpdateSetupStatus() {
        GamerProfile profile = createProfile();
        profile.setSetupStatus(GamerProfileSetupStatus.PARTIAL);

        assertThat(profile.getSetupStatus()).isEqualTo(GamerProfileSetupStatus.PARTIAL);
    }

    @Test
    void shouldUpdatePlaystyle() {
        GamerProfile profile = createProfile();
        profile.setPlaystyle(Playstyle.TEAM_ORIENTED);

        assertThat(profile.getPlaystyle()).isEqualTo(Playstyle.TEAM_ORIENTED);
    }

    @Test
    void shouldUpdateRoles() {
        GamerProfile profile = createProfile();
        profile.setMainRole(PlayerRole.SUPPORT);
        profile.setSecondaryRole(PlayerRole.IGL);

        assertThat(profile.getMainRole()).isEqualTo(PlayerRole.SUPPORT);
        assertThat(profile.getSecondaryRole()).isEqualTo(PlayerRole.IGL);
    }

    @Test
    void shouldTrackLookingForTeamFlag() {
        GamerProfile profile = createProfile();
        profile.setLookingForTeam(true);

        assertThat(profile.getLookingForTeam()).isTrue();
    }

    @Test
    void shouldTrackLookingForDuoFlag() {
        GamerProfile profile = createProfile();
        profile.setLookingForDuo(true);

        assertThat(profile.getLookingForDuo()).isTrue();
    }

    @Test
    void shouldHandlePendingSetupStatus() {
        GamerProfile profile = createProfile();
        profile.setSetupStatus(GamerProfileSetupStatus.PENDING);

        assertThat(profile.getSetupStatus()).isEqualTo(GamerProfileSetupStatus.PENDING);
    }

    @Test
    void shouldMaintainUserRelationship() {
        User user = createUser();
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .nickname("TestProfile")
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        assertThat(profile.getUser()).isEqualTo(user);
        assertThat(profile.getUser().getUsername()).isEqualTo(user.getUsername());
    }

    private GamerProfile createProfile() {
        return GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("TestPlayer")
                .bio("Test bio")
                .country("USA")
                .mainRole(PlayerRole.RIFLER)
                .secondaryRole(PlayerRole.SUPPORT)
                .premierRating(2000)
                .faceitLevel(7)
                .gcRank(5)
                .playstyle(Playstyle.CASUAL)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .lookingForTeam(false)
                .lookingForDuo(false)
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("gamerprofile" + System.nanoTime())
                .email("gamer" + System.nanoTime() + "@example.com")
                .firstName("Gamer")
                .lastName("Profile")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
