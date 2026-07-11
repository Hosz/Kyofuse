package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GamerProfilePartiallyFilledSetupServiceTest {

    private final GamerProfilePartiallyFilledSetupService service = new GamerProfilePartiallyFilledSetupService();

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenBioIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setBio("Some bio text");

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenCountryIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setCountry("Brazil");

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenStateIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setState("São Paulo");

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenCityIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setCity("São Paulo");

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenAvatarUrlIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setAvatarUrl("https://example.com/avatar.jpg");

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenMainRoleIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setMainRole(PlayerRole.RIFLER);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenSecondaryRoleIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setSecondaryRole(PlayerRole.AWP);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenPremierRatingIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setPremierRating(2500);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenFaceitLevelIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setFaceitLevel(5);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenGcRankIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setGcRank(5);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenPlaystyleIsSet() {
        GamerProfile profile = new GamerProfile();
        profile.setPlaystyle(Playstyle.AGGRESSIVE);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenLookingForTeamIsTrue() {
        GamerProfile profile = new GamerProfile();
        profile.setLookingForTeam(true);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_whenLookingForDuoIsTrue() {
        GamerProfile profile = new GamerProfile();
        profile.setLookingForDuo(true);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnFalse_whenAllFieldsAreNull() {
        GamerProfile profile = new GamerProfile();

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isFalse();
    }

    @Test
    void isPartiallyFilled_shouldReturnFalse_whenAllFieldsAreEmpty() {
        GamerProfile profile = new GamerProfile();
        profile.setBio("");
        profile.setCountry("");
        profile.setState("");
        profile.setCity("");
        profile.setAvatarUrl("");
        profile.setLookingForTeam(false);
        profile.setLookingForDuo(false);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isFalse();
    }

    @Test
    void isPartiallyFilled_shouldReturnFalse_whenBlankStringsProvided() {
        GamerProfile profile = new GamerProfile();
        profile.setBio("   ");
        profile.setCountry("   ");

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isFalse();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_withMultipleFieldsFilled() {
        GamerProfile profile = new GamerProfile();
        profile.setBio("Player bio");
        profile.setCountry("Brazil");
        profile.setMainRole(PlayerRole.SUPPORT);
        profile.setPremierRating(1500);
        profile.setLookingForTeam(true);

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }

    @Test
    void isPartiallyFilled_shouldReturnTrue_withNicknameNotRequired() {
        GamerProfile profile = new GamerProfile();
        profile.setBio("Some bio");

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isTrue();
    }
}
