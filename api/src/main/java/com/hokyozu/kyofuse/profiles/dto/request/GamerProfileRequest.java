package com.hokyozu.kyofuse.profiles.dto.request;

import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GamerProfileRequest(

        @Size(min = 1, max = 40)
        String nickname,

        @Size(max = 500)
        String bio,

        @Size(max = 500)
        String avatarUrl,

        @Size(max = 80)
        String country,

        @Size(max = 80)
        String city,

        @Size(max = 80)
        String state,

        PlayerRole mainRole,
        PlayerRole secondaryRole,

        @Min(1000)
        @Max(40000)
        Integer premierRating,

        @Min(0)
        @Max(10)
        Integer faceitLevel,

        @Min(0)
        @Max(21)
        Integer gcRank,

        Playstyle playstyle,

        Boolean lookingForTeam,
        Boolean lookingForDuo,

        @Size(max = 3, message = "You can choose three favorite maps.")
        List<Cs2Map> favoriteMaps
) {
}
