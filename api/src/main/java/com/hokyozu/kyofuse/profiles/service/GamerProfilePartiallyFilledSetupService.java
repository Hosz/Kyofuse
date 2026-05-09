package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import org.springframework.stereotype.Service;
import static com.hokyozu.kyofuse.profiles.service.GamerProfileSetupStatusResolverService.hasText;

@Service
public class GamerProfilePartiallyFilledSetupService {

    public GamerProfilePartiallyFilledSetupService() {
    }

    public static boolean isPartiallyFilled(GamerProfile profile) {
        return hasText(profile.getBio())
                || hasText(profile.getCountry())
                || hasText(profile.getState())
                || hasText(profile.getCity())
                || hasText(profile.getAvatarUrl())
                || profile.getMainRole() != null
                || profile.getSecondaryRole() != null
                || profile.getPremierRating() != null
                || profile.getFaceitLevel() != null
                || profile.getGcRank() != null
                || profile.getPlaystyle() != null
                || Boolean.TRUE.equals(profile.getLookingForTeam())
                || Boolean.TRUE.equals(profile.getLookingForDuo());
    }
}
