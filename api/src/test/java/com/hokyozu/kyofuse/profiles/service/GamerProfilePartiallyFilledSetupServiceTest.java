package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GamerProfilePartiallyFilledSetupServiceTest {

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenBioFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .bio("Competitive player")
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenCountryFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .country("USA")
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenMainRoleFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .mainRole(PlayerRole.AWPER)
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenSecondaryRoleFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .secondaryRole(PlayerRole.RIFLER)
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenPremierRatingFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .premierRating(2500)
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenFaceitLevelFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .faceitLevel(8)
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenPlaystyleFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenLookingForTeamFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .lookingForTeam(true)
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenLookingForDuoFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .lookingForDuo(true)
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnFalse_whenAllFieldsEmpty() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .bio(null)
                .country(null)
                .state(null)
                .city(null)
                .avatarUrl(null)
                .mainRole(null)
                .secondaryRole(null)
                .premierRating(null)
                .faceitLevel(null)
                .gcRank(null)
                .playstyle(null)
                .lookingForTeam(false)
                .lookingForDuo(false)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isFalse();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenStateFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .state("California")
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenCityFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .city("Los Angeles")
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenAvatarUrlFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .avatarUrl("https://example.com/avatar.jpg")
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenGcRankFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .gcRank(5)
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_withMultipleFields() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .bio("Competitive player")
                .country("USA")
                .mainRole(PlayerRole.SUPPORT)
                .premierRating(1800)
                .playstyle(Playstyle.TEAM_ORIENTED)
                .setupStatus(GamerProfileSetupStatus.PARTIAL)
                .build();

        boolean result = GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile);

        assertThat(result).isTrue();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("user" + System.nanoTime())
                .email("email" + System.nanoTime() + "@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
