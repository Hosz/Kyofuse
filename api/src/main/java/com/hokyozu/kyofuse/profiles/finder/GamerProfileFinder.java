package com.hokyozu.kyofuse.profiles.finder;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GamerProfileFinder {

    private final GamerProfileRepository gamerProfileRepository;

    public GamerProfile findProfileByUserId(UUID userId) {
        return gamerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Gamer profile not found for user ID: " + userId));
    }

    /** Busca em lote; ids sem perfil correspondente ficam de fora do resultado. */
    public List<GamerProfile> findAllByUserIds(List<UUID> userIds) {
        return userIds.isEmpty() ? List.of() : gamerProfileRepository.findByUserIdIn(userIds);
    }

    public GamerProfile findProfileById(UUID profileId) {
        return gamerProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Gamer profile not found for profile ID: " + profileId));
    }
}
