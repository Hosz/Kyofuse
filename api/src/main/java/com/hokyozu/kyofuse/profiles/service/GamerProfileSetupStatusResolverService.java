package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.hokyozu.kyofuse.profiles.service.GamerProfileCompleteSetupService.isCompleted;
import static com.hokyozu.kyofuse.profiles.service.GamerProfilePartiallyFilledSetupService.isPartiallyFilled;

@Service
@RequiredArgsConstructor
public class GamerProfileSetupStatusResolverService {

    public GamerProfileSetupStatus resolve(GamerProfile profile) {
        if (isCompleted(profile)) {
            return GamerProfileSetupStatus.COMPLETED;
        }

        if (isPartiallyFilled(profile)) {
            return GamerProfileSetupStatus.PARTIAL;
        }

        return GamerProfileSetupStatus.PENDING;
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
