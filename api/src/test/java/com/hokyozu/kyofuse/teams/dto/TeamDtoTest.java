package com.hokyozu.kyofuse.teams.dto;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.dto.request.TeamFilter;
import com.hokyozu.kyofuse.teams.dto.request.TeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequest;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TeamDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void teamRequestAcceptsValidPayload() {
        TeamRequest request = new TeamRequest(
                "Kyofuse Academy",
                null,
                null,
                "kyofuse-academy",
                "Development team",
                "BR",
                1000,
                40000,
                1,
                10,
                1,
                21,
                List.of(PlayerRole.AWPER, PlayerRole.RIFLER)
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void teamRequestRejectsInvalidSlugAndRanges() {
        TeamRequest request = new TeamRequest(
                "Kyofuse Academy",
                null,
                null,
                "Kyofuse Academy!",
                "Development team",
                "BR",
                40000,
                1000,
                10,
                1,
                21,
                1,
                List.of(PlayerRole.AWPER)
        );

        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "slug",
                        "premierRatingRangeValid",
                        "faceitLevelRangeValid",
                        "gcRankRangeValid"
                );
    }

    @Test
    void teamRequestRejectsNullRequiredRoleItem() {
        TeamRequest request = new TeamRequest(
                "Kyofuse Academy",
                null,
                null,
                "kyofuse-academy",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Arrays.asList(PlayerRole.AWPER, null)
        );

        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("requiredRoles[1].<list element>");
    }

    @Test
    void teamRequestAcceptsOpenEndedRanges() {
        TeamRequest maxOnlyRequest = new TeamRequest(
                "Kyofuse Academy",
                null,
                null,
                "kyofuse-academy",
                null,
                null,
                null,
                40000,
                null,
                10,
                null,
                21,
                null
        );
        TeamRequest minOnlyRequest = new TeamRequest(
                "Kyofuse Academy",
                null,
                null,
                "kyofuse-academy",
                null,
                null,
                1000,
                null,
                1,
                null,
                1,
                null,
                null
        );

        assertThat(maxOnlyRequest.isPremierRatingRangeValid()).isTrue();
        assertThat(maxOnlyRequest.isFaceitLevelRangeValid()).isTrue();
        assertThat(maxOnlyRequest.isGcRankRangeValid()).isTrue();
        assertThat(minOnlyRequest.isPremierRatingRangeValid()).isTrue();
        assertThat(minOnlyRequest.isFaceitLevelRangeValid()).isTrue();
        assertThat(minOnlyRequest.isGcRankRangeValid()).isTrue();
        assertThat(validator.validate(maxOnlyRequest)).isEmpty();
        assertThat(validator.validate(minOnlyRequest)).isEmpty();
    }

    @Test
    void teamFilterReturnsNullStatusWhenNotProvided() {
        TeamFilter filter = new TeamFilter(
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
        );

        assertThat(filter.statusOrActive()).isNull();
        assertThat(validator.validate(filter)).isEmpty();
    }

    @Test
    void teamFilterKeepsProvidedStatus() {
        TeamFilter filter = new TeamFilter(
                null,
                null,
                TeamStatus.INACTIVE,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThat(filter.statusOrActive()).isEqualTo(TeamStatus.INACTIVE);
        assertThat(validator.validate(filter)).isEmpty();
    }

    @Test
    void teamFilterRejectsOutOfRangeAndInvalidRanges() {
        TeamFilter filter = new TeamFilter(
                null,
                null,
                null,
                null,
                null,
                999,
                500,
                11,
                1,
                22,
                1
        );

        Set<ConstraintViolation<TeamFilter>> violations = validator.validate(filter);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "minPremierRating",
                        "maxPremierRating",
                        "minFaceitLevel",
                        "minGcRank",
                        "premierRatingRangeValid",
                        "faceitLevelRangeValid",
                        "gcRankRangeValid"
                );
    }

    @Test
    void teamFilterRejectsNullRequiredRoleItem() {
        TeamFilter filter = new TeamFilter(
                null,
                null,
                null,
                null,
                Arrays.asList(PlayerRole.AWPER, null),
                null,
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<TeamFilter>> violations = validator.validate(filter);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("requiredRoles[1].<list element>");
    }

    @Test
    void teamFilterAcceptsOpenEndedRanges() {
        TeamFilter maxOnlyFilter = new TeamFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                40000,
                null,
                10,
                null,
                21
        );
        TeamFilter minOnlyFilter = new TeamFilter(
                null,
                null,
                null,
                null,
                null,
                1000,
                null,
                1,
                null,
                1,
                null
        );

        assertThat(maxOnlyFilter.isPremierRatingRangeValid()).isTrue();
        assertThat(maxOnlyFilter.isFaceitLevelRangeValid()).isTrue();
        assertThat(maxOnlyFilter.isGcRankRangeValid()).isTrue();
        assertThat(minOnlyFilter.isPremierRatingRangeValid()).isTrue();
        assertThat(minOnlyFilter.isFaceitLevelRangeValid()).isTrue();
        assertThat(minOnlyFilter.isGcRankRangeValid()).isTrue();
        assertThat(validator.validate(maxOnlyFilter)).isEmpty();
        assertThat(validator.validate(minOnlyFilter)).isEmpty();
    }

    @Test
    void teamFilterAcceptsValidClosedRanges() {
        TeamFilter filter = new TeamFilter(
                null,
                null,
                null,
                null,
                null,
                1000,
                40000,
                1,
                10,
                1,
                21
        );

        assertThat(filter.isPremierRatingRangeValid()).isTrue();
        assertThat(filter.isFaceitLevelRangeValid()).isTrue();
        assertThat(filter.isGcRankRangeValid()).isTrue();
        assertThat(validator.validate(filter)).isEmpty();
    }

    @Test
    void updateTeamRequestAcceptsPartialValidPayload() {
        UpdateTeamRequest request = new UpdateTeamRequest(
                "Kyofuse Academy",
                null,
                null,
                null,
                "BR",
                null,
                40000,
                null,
                10,
                null,
                21,
                TeamStatus.ACTIVE
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void updateTeamRequestRejectsInvalidFieldsAndRanges() {
        UpdateTeamRequest request = new UpdateTeamRequest(
                "",
                "a".repeat(501),
                null,
                null,
                "",
                40000,
                1000,
                10,
                1,
                21,
                1,
                TeamStatus.ACTIVE
        );

        Set<ConstraintViolation<UpdateTeamRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "description",
                        "premierRatingRangeValid",
                        "faceitLevelRangeValid",
                        "gcRankRangeValid"
                );
    }

    @Test
    void updateTeamRequestAcceptsOpenEndedRanges() {
        UpdateTeamRequest maxOnlyRequest = new UpdateTeamRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                40000,
                null,
                10,
                null,
                21,
                null
        );
        UpdateTeamRequest minOnlyRequest = new UpdateTeamRequest(
                null,
                null,
                null,
                null,
                null,
                1000,
                null,
                1,
                null,
                1,
                null,
                null
        );

        assertThat(maxOnlyRequest.isPremierRatingRangeValid()).isTrue();
        assertThat(maxOnlyRequest.isFaceitLevelRangeValid()).isTrue();
        assertThat(maxOnlyRequest.isGcRankRangeValid()).isTrue();
        assertThat(minOnlyRequest.isPremierRatingRangeValid()).isTrue();
        assertThat(minOnlyRequest.isFaceitLevelRangeValid()).isTrue();
        assertThat(minOnlyRequest.isGcRankRangeValid()).isTrue();
        assertThat(validator.validate(maxOnlyRequest)).isEmpty();
        assertThat(validator.validate(minOnlyRequest)).isEmpty();
    }

    @Test
    void updateTeamRequestAcceptsValidClosedRanges() {
        UpdateTeamRequest request = new UpdateTeamRequest(
                null,
                null,
                null,
                null,
                null,
                1000,
                40000,
                1,
                10,
                1,
                21,
                null
        );

        assertThat(request.isPremierRatingRangeValid()).isTrue();
        assertThat(request.isFaceitLevelRangeValid()).isTrue();
        assertThat(request.isGcRankRangeValid()).isTrue();
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void teamRequestAcceptsCreateCommunityFlag() {
        TeamRequest requestTrue = new TeamRequest("Kyofuse Academy", null, null, "kyofuse-academy", null, null, null, null, null, null, null, null, null, true);
        TeamRequest requestFalse = new TeamRequest("Kyofuse Academy", null, null, "kyofuse-academy", null, null, null, null, null, null, null, null, null, false);
        TeamRequest requestNull = new TeamRequest("Kyofuse Academy", null, null, "kyofuse-academy", null, null, null, null, null, null, null, null, null, null);

        assertThat(validator.validate(requestTrue)).isEmpty();
        assertThat(validator.validate(requestFalse)).isEmpty();
        assertThat(validator.validate(requestNull)).isEmpty();
        assertThat(requestTrue.createCommunity()).isTrue();
        assertThat(requestFalse.createCommunity()).isFalse();
        assertThat(requestNull.createCommunity()).isNull();
    }
}
