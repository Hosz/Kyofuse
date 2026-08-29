package com.hokyozu.kyofuse.profiles.mapper;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GamerProfileMapperTest {

    @Test
    void updateEntityWithNullFieldsKeepsExistingValuesAndUpdatesTimestamp() {
        Instant previousUpdatedAt = Instant.parse("2026-01-01T00:00:00Z");
        GamerProfile profile = GamerProfile.builder()
                .nickname("player")
                .bio("bio")
                .avatarUrl("avatar")
                .bannerUrl("banner")
                .country("BR")
                .state("SP")
                .city("Sao Paulo")
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(PlayerRole.RIFLER)
                .premierRating(15000)
                .faceitLevel(8)
                .gcRank(18)
                .playstyle(Playstyle.COMPETITIVE)
                .lookingForTeam(true)
                .lookingForDuo(false)
                .updatedAt(previousUpdatedAt)
                .build();

        GamerProfileMapper.updateEntity(profile, new GamerProfileRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(profile.getNickname()).isEqualTo("player");
        assertThat(profile.getBio()).isEqualTo("bio");
        assertThat(profile.getAvatarUrl()).isEqualTo("avatar");
        assertThat(profile.getBannerUrl()).isEqualTo("banner");
        assertThat(profile.getCountry()).isEqualTo("BR");
        assertThat(profile.getState()).isEqualTo("SP");
        assertThat(profile.getCity()).isEqualTo("Sao Paulo");
        assertThat(profile.getMainRole()).isEqualTo(PlayerRole.AWPER);
        assertThat(profile.getSecondaryRole()).isEqualTo(PlayerRole.RIFLER);
        assertThat(profile.getPremierRating()).isEqualTo(15000);
        assertThat(profile.getFaceitLevel()).isEqualTo(8);
        assertThat(profile.getGcRank()).isEqualTo(18);
        assertThat(profile.getPlaystyle()).isEqualTo(Playstyle.COMPETITIVE);
        assertThat(profile.getLookingForTeam()).isTrue();
        assertThat(profile.getLookingForDuo()).isFalse();
        assertThat(profile.getUpdatedAt()).isAfter(previousUpdatedAt);
    }

    @Test
    void updateFromSteamFillsOnlyMissingOrFallbackFields() {
        GamerProfile profile = GamerProfile.builder()
                .nickname("steam_123456")
                .avatarUrl(null)
                .country(null)
                .build();

        boolean changed = GamerProfileMapper.updateFromSteam(profile, "ProGamer", "https://avatar.url", "BR");

        assertThat(changed).isTrue();
        assertThat(profile.getNickname()).isEqualTo("ProGamer");
        assertThat(profile.getAvatarUrl()).isEqualTo("https://avatar.url");
        assertThat(profile.getCountry()).isEqualTo("BR");
    }

    @Test
    void updateFromSteamDoesNotOverwriteExistingCustomData() {
        GamerProfile profile = GamerProfile.builder()
                .nickname("CustomNick")
                .avatarUrl("https://custom.avatar")
                .country("US")
                .build();

        boolean changed = GamerProfileMapper.updateFromSteam(profile, "SteamNick", "https://steam.avatar", "BR");

        assertThat(changed).isFalse();
        assertThat(profile.getNickname()).isEqualTo("CustomNick");
        assertThat(profile.getAvatarUrl()).isEqualTo("https://custom.avatar");
        assertThat(profile.getCountry()).isEqualTo("US");
    }
}
