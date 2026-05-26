package com.hokyozu.kyofuse.profiles.finder;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GamerProfileFinder {

    private final GamerProfileRepository gamerProfileRepository;

    public GamerProfile findProfileByUserId(UUID userId) {
        return gamerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Gamer profile not found for user ID: " + userId));
    }

    public GamerProfile findProfileById(UUID profileId) {
        return gamerProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Gamer profile not found for profile ID: " + profileId));
    }
}
