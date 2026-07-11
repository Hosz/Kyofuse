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

class GamerProfileCompleteSetupServiceTest {

    @Test
    void isCompleted_shouldReturnTrue_whenAllRequiredFieldsFilled() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("ProPlayer")
                .bio("Competitive player")
                .country("USA")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(2500)
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenNicknameIsMissing() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname(null)
                .bio("Competitive player")
                .country("USA")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(2500)
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenBioIsMissing() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("ProPlayer")
                .bio(null)
                .country("USA")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(2500)
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenCountryIsMissing() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("ProPlayer")
                .bio("Competitive player")
                .country(null)
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(2500)
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenMainRoleIsMissing() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("ProPlayer")
                .bio("Competitive player")
                .country("USA")
                .mainRole(null)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(2500)
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenSecondaryRoleIsMissing() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("ProPlayer")
                .bio("Competitive player")
                .country("USA")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(null)
                .premierRating(2500)
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenRatingIsMissing() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("ProPlayer")
                .bio("Competitive player")
                .country("USA")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(null)
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenPlaystyleIsMissing() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("ProPlayer")
                .bio("Competitive player")
                .country("USA")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(2500)
                .playstyle(null)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenEmptyStringNickname() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("")
                .bio("Competitive player")
                .country("USA")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(2500)
                .playstyle(Playstyle.COMPETITIVE)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isFalse();
    }

    @Test
    void isCompleted_shouldReturnTrue_withDifferentRoles() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("SupportMain")
                .bio("Support specialist")
                .country("Canada")
                .mainRole(PlayerRole.SUPPORT)
                .secondaryRole(PlayerRole.IGL)
                .premierRating(1800)
                .playstyle(Playstyle.TEAM_ORIENTED)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

        assertThat(result).isTrue();
    }

    @Test
    void isCompleted_shouldReturnTrue_withDifferentPlaystyles() {
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(createUser())
                .nickname("CasualPlayer")
                .bio("Just for fun")
                .country("UK")
                .mainRole(PlayerRole.RIFLER)
                .secondaryRole(PlayerRole.FLEX)
                .premierRating(1200)
                .playstyle(Playstyle.CASUAL)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .build();

        boolean result = GamerProfileCompleteSetupService.isCompleted(profile);

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
