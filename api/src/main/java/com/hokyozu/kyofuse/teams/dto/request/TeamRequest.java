package com.hokyozu.kyofuse.teams.dto.request;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TeamRequest(
        @NotBlank
        @Size(max = 80)
        String name,

        @Size(max = 500)
        @Pattern(regexp = "^(https?://.+)?$", message = "Avatar URL must be a valid HTTP or HTTPS URL")
        String avatarUrl,

        @Size(max = 500)
        @Pattern(regexp = "^(https?://.+)?$", message = "Banner URL must be a valid HTTP or HTTPS URL")
        String bannerUrl,

        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain only lowercase letters, numbers and single hyphens")
        String slug,

        @Size(max = 500)
        String description,
        String region,

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
        Integer maxGcRank,
        List<@NotNull PlayerRole> requiredRoles,
        Boolean createCommunity
) {
        public TeamRequest(
                String name,
                String avatarUrl,
                String bannerUrl,
                String slug,
                String description,
                String region,
                Integer minPremierRating,
                Integer maxPremierRating,
                Integer minFaceitLevel,
                Integer maxFaceitLevel,
                Integer minGcRank,
                Integer maxGcRank,
                List<@NotNull PlayerRole> requiredRoles
        ) {
                this(name, avatarUrl, bannerUrl, slug, description, region, minPremierRating, maxPremierRating, minFaceitLevel, maxFaceitLevel, minGcRank, maxGcRank, requiredRoles, null);
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
