package com.hokyozu.kyofuse.profiles.dto;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileMinRequest;
import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GamerProfileDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void gamerProfileRequestAcceptsValidPayload() {
        GamerProfileRequest request = new GamerProfileRequest(
                "player",
                "bio",
                "https://example.com/avatar.png",
                "https://example.com/banner.png",
                "Brazil",
                "Sao Paulo",
                "SP",
                true,
                PlayerRole.AWPER,
                PlayerRole.RIFLER,
                15000,
                8,
                18,
                Playstyle.COMPETITIVE,
                true,
                false,
                List.of(Cs2Map.MIRAGE, Cs2Map.INFERNO, Cs2Map.NUKE)
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void gamerProfileRequestRejectsDangerousProtocolsInUrls() {
        GamerProfileRequest request = new GamerProfileRequest(
                "player",
                "bio",
                "javascript:alert('xss')",
                "data:text/html,<script>alert(1)</script>",
                "Brazil",
                "Sao Paulo",
                "SP",
                true,
                PlayerRole.AWPER,
                PlayerRole.RIFLER,
                15000,
                8,
                18,
                Playstyle.COMPETITIVE,
                true,
                false,
                List.of(Cs2Map.MIRAGE)
        );

        Set<ConstraintViolation<GamerProfileRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("avatarUrl", "bannerUrl");
    }

    @Test
    void gamerProfileRequestRejectsOutOfRangeAndOversizedFields() {
        GamerProfileRequest request = new GamerProfileRequest(
                "",
                "a".repeat(501),
                "a".repeat(501),
                "a".repeat(501),
                "a".repeat(81),
                "a".repeat(81),
                "a".repeat(81),
                null,
                null,
                null,
                999,
                11,
                22,
                null,
                null,
                null,
                List.of(Cs2Map.MIRAGE, Cs2Map.INFERNO, Cs2Map.NUKE, Cs2Map.TRAIN)
        );

        Set<ConstraintViolation<GamerProfileRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "nickname",
                        "bio",
                        "avatarUrl",
                        "bannerUrl",
                        "country",
                        "city",
                        "state",
                        "premierRating",
                        "faceitLevel",
                        "gcRank",
                        "favoriteMaps"
                );
    }

    @Test
    void gamerProfileMinRequestCanBeConstructed() {
        GamerProfileMinRequest request = new GamerProfileMinRequest();

        assertThat(request).isNotNull();
    }

    @Test
    void gamerProfileResponseExposesRecordValues() {
        UUID profileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        GamerProfileResponse response = new GamerProfileResponse(
                profileId,
                userId,
                "player",
                "player",
                "bio",
                "avatar",
                "banner",
                "BR",
                "Sao Paulo",
                "SP",
                true,
                PlayerRole.AWPER,
                PlayerRole.RIFLER,
                15000,
                8,
                18,
                Playstyle.COMPETITIVE,
                true,
                false,
                GamerProfileSetupStatus.COMPLETED,
                List.of(Cs2Map.MIRAGE),
                now,
                now
        );

        assertThat(response.id()).isEqualTo(profileId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.favoriteMaps()).containsExactly(Cs2Map.MIRAGE);
        assertThat(response.setupStatus()).isEqualTo(GamerProfileSetupStatus.COMPLETED);
    }
}
