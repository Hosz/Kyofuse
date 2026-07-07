package com.hokyozu.kyofuse.teams.dto.request;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TeamFilter(
        String name,
        String slug,
        TeamStatus status,
        String region,
        List<@NotNull PlayerRole> requiredRoles,

        @Min(1000)
        @Max(40000)
        Integer minPremierRating,

        @Min(1000)
        @Max(40000)
        Integer maxPremierRating,

        @Min(1)
        @Max(10)
        Integer minFaceitLevel,

        @Min(1)
        @Max(10)
        Integer maxFaceitLevel,

        @Min(1)
        @Max(21)
        Integer minGcRank,

        @Min(1)
        @Max(21)
        Integer maxGcRank
) {
    public TeamStatus statusOrActive() {
        return status == null ? TeamStatus.ACTIVE : status;
    }

    @AssertTrue(message = "minPremierRating must be less than or equal to maxPremierRating")
    public boolean isPremierRatingRangeValid() {
        return minPremierRating == null || maxPremierRating == null || minPremierRating <= maxPremierRating;
    }

    @AssertTrue(message = "minFaceitLevel must be less than or equal to maxFaceitLevel")
    public boolean isFaceitLevelRangeValid() {
        return minFaceitLevel == null || maxFaceitLevel == null || minFaceitLevel <= maxFaceitLevel;
    }

    @AssertTrue(message = "minGcRank must be less than or equal to maxGcRank")
    public boolean isGcRankRangeValid() {
        return minGcRank == null || maxGcRank == null || minGcRank <= maxGcRank;
    }
}
