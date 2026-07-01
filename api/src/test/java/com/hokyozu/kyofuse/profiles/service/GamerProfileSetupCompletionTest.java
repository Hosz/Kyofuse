package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GamerProfileSetupCompletionTest {

    @Test
    void emptyProfileIsNeitherPartialNorComplete() {
        GamerProfile profile = GamerProfile.builder().build();

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isFalse();
        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void eachOptionalFieldMakesProfilePartial() {
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(
                GamerProfile.builder().city("São Paulo").build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(
                GamerProfile.builder().lookingForTeam(true).build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(
                GamerProfile.builder().faceitLevel(8).build())).isTrue();
    }

    @Test
    void falseFlagsAndBlankTextDoNotMakeProfilePartial() {
        GamerProfile profile = GamerProfile.builder()
                .bio(" ")
                .lookingForTeam(false)
                .lookingForDuo(false)
                .build();

        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(profile)).isFalse();
    }

    @Test
    void completeProfileRequiresEveryRequiredField() {
        GamerProfile profile = completeProfile();

        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isTrue();

        profile.setBio(null);
        assertThat(GamerProfileCompleteSetupService.isCompleted(profile)).isFalse();
    }

    @Test
    void servicesCanBeInstantiated() {
        assertThat(new GamerProfilePartiallyFilledSetupService()).isNotNull();
        assertThat(new GamerProfileCompleteSetupService()).isNotNull();
    }

    private GamerProfile completeProfile() {
        return GamerProfile.builder()
                .nickname("player")
                .bio("Entry fragger")
                .country("BR")
                .mainRole(PlayerRole.RIFLER)
                .secondaryRole(PlayerRole.AWPER)
                .premierRating(15000)
                .playstyle(Playstyle.COMPETITIVE)
                .build();
    }
}
