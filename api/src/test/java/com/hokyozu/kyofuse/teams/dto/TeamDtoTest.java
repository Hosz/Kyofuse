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
    void teamFilterDefaultsStatusToActive() {
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

        assertThat(filter.statusOrActive()).isEqualTo(TeamStatus.ACTIVE);
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
    void updateTeamRequestAcceptsPartialValidPayload() {
        UpdateTeamRequest request = new UpdateTeamRequest(
                "Kyofuse Academy",
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
                        "name",
                        "description",
                        "region",
                        "premierRatingRangeValid",
                        "faceitLevelRangeValid",
                        "gcRankRangeValid"
                );
    }
}
