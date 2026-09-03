package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.steam.SteamPlayerSummary;
import com.hokyozu.kyofuse.profiles.mapper.GamerProfileMapper;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SteamProfileSyncService {

    private final GamerProfileRepository gamerProfileRepository;
    private final UserRepository userRepository;

    @CacheEvict(value = "user_profiles", key = "'id:' + #user.id")
    @Transactional
    public void syncIfMissing(User user, SteamPlayerSummary summary) {
        if (summary == null || user == null) {
            return;
        }

        gamerProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            boolean changed = GamerProfileMapper.updateFromSteam(
                    profile,
                    summary.personaName(),
                    summary.avatarFull(),
                    summary.locCountryCode()
            );

            if (changed) {
                profile.setUpdatedAt(Instant.now());
                gamerProfileRepository.save(profile);
            }
        });

        if (user.getFirstName() != null && user.getFirstName().startsWith("steam_") && summary.personaName() != null && !summary.personaName().isBlank()) {
            user.setFirstName(summary.personaName().trim());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        }
    }
}
