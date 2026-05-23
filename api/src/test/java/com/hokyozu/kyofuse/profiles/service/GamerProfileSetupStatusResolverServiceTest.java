package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GamerProfileSetupStatusResolverServiceTest {

    private final GamerProfileSetupStatusResolverService resolver = new GamerProfileSetupStatusResolverService();

    @Test
    void resolveReturnsCompletedWhenRequiredFieldsAreFilled() {
        GamerProfile profile = GamerProfile.builder()
                .nickname("player")
                .bio("bio")
                .country("BR")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(15000)
                .playstyle(Playstyle.COMPETITIVE)
                .build();

        assertThat(resolver.resolve(profile)).isEqualTo(GamerProfileSetupStatus.COMPLETED);
    }

    @Test
    void resolveReturnsPartialWhenSomeOptionalSetupFieldsAreFilled() {
        GamerProfile profile = GamerProfile.builder()
                .nickname("player")
                .lookingForDuo(true)
                .build();

        assertThat(resolver.resolve(profile)).isEqualTo(GamerProfileSetupStatus.PARTIAL);
    }

    @Test
    void resolveReturnsPendingWhenOnlyMinimalProfileExists() {
        GamerProfile profile = GamerProfile.builder()
                .nickname("player")
                .lookingForDuo(false)
                .lookingForTeam(false)
                .build();

        assertThat(resolver.resolve(profile)).isEqualTo(GamerProfileSetupStatus.PENDING);
    }

    @Test
    void setupServiceConstructorsCanBeCreatedBySpring() {
        assertThat(new GamerProfileCompleteSetupService()).isNotNull();
        assertThat(new GamerProfilePartiallyFilledSetupService()).isNotNull();
    }

    @Test
    void isCompletedReturnsFalseWhenAnyRequiredFieldIsMissing() {
        assertThat(GamerProfileCompleteSetupService.isCompleted(completedProfile().nickname(null).build())).isFalse();
        assertThat(GamerProfileCompleteSetupService.isCompleted(completedProfile().bio(" ").build())).isFalse();
        assertThat(GamerProfileCompleteSetupService.isCompleted(completedProfile().country(null).build())).isFalse();
        assertThat(GamerProfileCompleteSetupService.isCompleted(completedProfile().mainRole(null).build())).isFalse();
        assertThat(GamerProfileCompleteSetupService.isCompleted(completedProfile().secondaryRole(null).build())).isFalse();
        assertThat(GamerProfileCompleteSetupService.isCompleted(completedProfile().premierRating(null).build())).isFalse();
        assertThat(GamerProfileCompleteSetupService.isCompleted(completedProfile().playstyle(null).build())).isFalse();
    }

    @Test
    void isPartiallyFilledReturnsTrueForEachPartialField() {
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().bio("bio").build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().country("BR").build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().state("SP").build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().city("Sao Paulo").build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().avatarUrl("avatar").build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().mainRole(PlayerRole.AWPER).build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().secondaryRole(PlayerRole.RIFLER).build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().premierRating(15000).build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().faceitLevel(8).build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().gcRank(18).build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().playstyle(Playstyle.COMPETITIVE).build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().lookingForTeam(true).build())).isTrue();
        assertThat(GamerProfilePartiallyFilledSetupService.isPartiallyFilled(GamerProfile.builder().lookingForDuo(true).build())).isTrue();
    }

    @Test
    void hasTextHandlesNullBlankAndText() {
        assertThat(GamerProfileSetupStatusResolverService.hasText(null)).isFalse();
        assertThat(GamerProfileSetupStatusResolverService.hasText(" ")).isFalse();
        assertThat(GamerProfileSetupStatusResolverService.hasText("text")).isTrue();
    }

    private static GamerProfile.GamerProfileBuilder completedProfile() {
        return GamerProfile.builder()
                .nickname("player")
                .bio("bio")
                .country("BR")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(15000)
                .playstyle(Playstyle.COMPETITIVE);
    }
}
