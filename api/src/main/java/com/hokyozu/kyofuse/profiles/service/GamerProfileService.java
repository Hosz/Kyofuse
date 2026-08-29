package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.profiles.mapper.GamerProfileMapper;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileFavoriteMapRepository;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
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
    private final ProfilePermissionService profilePermissionService;

    private final GamerProfileFinder gamerProfileFinder;
    private final UserFinder userFinder;

    @Transactional
    public void createGamerProfileMin(User user) {
        GamerProfile minProfile = GamerProfileMapper.toEntity(user);
        gamerProfileRepository.save(minProfile);
    }

    @Transactional
    public void createGamerProfile(User user, String nickname, String avatarUrl, String country) {
        GamerProfile profile = GamerProfileMapper.toEntity(user, nickname, avatarUrl, country);
        gamerProfileRepository.save(profile);
    }

    @Transactional
    public GamerProfileResponse editProfile(UUID userId, GamerProfileRequest request) {

        if (request.nickname() != null && request.nickname().trim().isEmpty()) {
            throw new UnauthorizedException("Nickname cannot be empty");
        }

        GamerProfile profile = gamerProfileFinder.findProfileByUserId(userId);

        GamerProfileMapper.updateEntity(profile, request);
        updateFavoriteMapsService.execute(profile, request.favoriteMaps());
        profile.setSetupStatus(setupStatusResolverService.resolve(profile));

        GamerProfile savedProfile = gamerProfileRepository.save(profile);
        List<GamerProfileFavoriteMap> favoriteMaps =
                gamerProfileFavoriteMapRepository.findByProfile_Id(savedProfile.getId());
        return GamerProfileMapper.toResponse(savedProfile, favoriteMaps);
    }

    @Transactional(readOnly = true)
    public GamerProfileResponse viewMyProfile(UUID userId) {
        GamerProfile gamerProfile = gamerProfileFinder.findProfileByUserId(userId);

        List<GamerProfileFavoriteMap> favoriteMaps =
                gamerProfileFavoriteMapRepository.findByProfile_Id(gamerProfile.getId());

        return GamerProfileMapper.toResponse(gamerProfile, favoriteMaps);
    }

    @Transactional(readOnly = true)
    public GamerProfileResponse viewUserProfile(UUID profileId, UUID userId) {
        User requestingUser = userFinder.findProfileByUserId(profileId);
        GamerProfile userRequestedProfile = gamerProfileFinder.findProfileByUserId(profileId);
        User user = userFinder.findProfileByUserId(userId);

        profilePermissionService.validateViewProfile(user, requestingUser);

        List<GamerProfileFavoriteMap> favoriteMaps =
                gamerProfileFavoriteMapRepository.findByProfile_Id(userRequestedProfile.getId());

        return GamerProfileMapper.toResponse(userRequestedProfile, favoriteMaps);
    }
}
