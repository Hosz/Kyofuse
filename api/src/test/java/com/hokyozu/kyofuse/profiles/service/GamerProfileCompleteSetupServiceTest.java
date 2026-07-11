package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GamerProfileCompleteSetupServiceTest {

    private final GamerProfileCompleteSetupService service = new GamerProfileCompleteSetupService();

    @Test
    void isCompleted_shouldReturnTrue_whenAllRequiredFieldsAreFilled() {
        GamerProfile profile = createCompleteProfile();

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isTrue();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenNicknameIsNull() {
        GamerProfile profile = createCompleteProfile();
        profile.setNickname(null);

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenNicknameIsEmpty() {
        GamerProfile profile = createCompleteProfile();
        profile.setNickname("");

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenNicknameIsBlank() {
        GamerProfile profile = createCompleteProfile();
        profile.setNickname("   ");

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenBioIsNull() {
        GamerProfile profile = createCompleteProfile();
        profile.setBio(null);

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenBioIsEmpty() {
        GamerProfile profile = createCompleteProfile();
        profile.setBio("");

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenCountryIsNull() {
        GamerProfile profile = createCompleteProfile();
        profile.setCountry(null);

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenCountryIsEmpty() {
        GamerProfile profile = createCompleteProfile();
        profile.setCountry("");

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenMainRoleIsNull() {
        GamerProfile profile = createCompleteProfile();
        profile.setMainRole(null);

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenSecondaryRoleIsNull() {
        GamerProfile profile = createCompleteProfile();
        profile.setSecondaryRole(null);

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenPremierRatingIsNull() {
        GamerProfile profile = createCompleteProfile();
        profile.setPremierRating(null);

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenPlaystyleIsNull() {
        GamerProfile profile = createCompleteProfile();
        profile.setPlaystyle(null);

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnFalse_whenMultipleFieldsAreMissing() {
        GamerProfile profile = createCompleteProfile();
        profile.setNickname(null);
        profile.setMainRole(null);
        profile.setPremierRating(null);

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void isCompleted_shouldReturnTrue_withWhitespaceInTextFields() {
        GamerProfile profile = new GamerProfile();
        profile.setNickname("Nick With Space");
        profile.setBio("Bio with multiple spaces");
        profile.setCountry("United States");
        profile.setMainRole(PlayerRole.ENTRY);
        profile.setSecondaryRole(PlayerRole.SUPPORT);
        profile.setPremierRating(3000);
        profile.setPlaystyle(Playstyle.AGGRESSIVE);

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isTrue();
    }

    private GamerProfile createCompleteProfile() {
        GamerProfile profile = new GamerProfile();
        profile.setNickname("ProPlayer");
        profile.setBio("Competitive player");
        profile.setCountry("Brazil");
        profile.setMainRole(PlayerRole.RIFLER);
        profile.setSecondaryRole(PlayerRole.AWP);
        profile.setPremierRating(2500);
        profile.setPlaystyle(Playstyle.TACTICAL);
        return profile;
    }
}
