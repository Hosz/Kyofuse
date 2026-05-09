package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.mapper.GamerProfileMapper;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileFavoriteMapRepository;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GamerProfileService {

    private final GamerProfileRepository gamerProfileRepository;
    private final GamerProfileFavoriteMapRepository gamerProfileFavoriteMapRepository;

    private final GamerProfileSetupStatusResolverService setupStatusResolverService;
    private final UpdateFavoriteMapsService updateFavoriteMapsService;

    @Transactional
    public void createGamerProfileMin(User user) {
        GamerProfile minProfile = GamerProfileMapper.toEntity(user);
        gamerProfileRepository.save(minProfile);
    }

    @Transactional
    public GamerProfileResponse editProfile(UUID userId, GamerProfileRequest request) {

        if (request.nickname() != null && request.nickname().trim().isEmpty()) {
            throw new UnauthorizedException("Nickname cannot be empty");
        }

        GamerProfile profile = gamerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Gamer profile not found for user ID: " + userId));

        GamerProfileMapper.updateEntity(profile, request);
        updateFavoriteMapsService.execute(profile, request.favoriteMaps());
        profile.setSetupStatus(setupStatusResolverService.resolve(profile));

        GamerProfile savedProfile = gamerProfileRepository.save(profile);
        List<GamerProfileFavoriteMap> favoriteMaps =
                gamerProfileFavoriteMapRepository.findByProfile_Id(savedProfile.getId());
        return GamerProfileMapper.toResponse(savedProfile, favoriteMaps);
    }

    public GamerProfileResponse viewMyProfile(UUID userId) {
        GamerProfile gamerProfile = gamerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Gamer profile not found for user ID: " + userId));

        List<GamerProfileFavoriteMap> favoriteMaps =
                gamerProfileFavoriteMapRepository.findByProfile_Id(gamerProfile.getId());

        return GamerProfileMapper.toResponse(gamerProfile, favoriteMaps);
    }

    public GamerProfileResponse viewUserProfile(UUID profileId) {
        GamerProfile gamerProfile = gamerProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Gamer profile not found for profile ID: " + profileId));

        List<GamerProfileFavoriteMap> favoriteMaps =
                gamerProfileFavoriteMapRepository.findByProfile_Id(gamerProfile.getId());

        return GamerProfileMapper.toResponse(gamerProfile, favoriteMaps);
    }
}
