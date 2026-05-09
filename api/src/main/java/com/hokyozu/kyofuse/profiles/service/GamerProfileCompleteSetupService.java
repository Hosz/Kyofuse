package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import org.springframework.stereotype.Service;

import static com.hokyozu.kyofuse.profiles.service.GamerProfileSetupStatusResolverService.hasText;

@Service
public class GamerProfileCompleteSetupService {

    public GamerProfileCompleteSetupService() {
    }

    public static boolean isCompleted(GamerProfile profile) {
        return hasText(profile.getNickname())
                && hasText(profile.getBio())
                && hasText(profile.getCountry())
                && profile.getMainRole() != null
                && profile.getSecondaryRole() != null
                && profile.getPremierRating() != null
                && profile.getPlaystyle() != null;
    }
}
