package com.hokyozu.kyofuse.profiles.mapper;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;
import java.util.List;

public class GamerProfileMapper {

    private GamerProfileMapper() {
    }

    public static void updateEntity(GamerProfile profile, GamerProfileRequest request) {
        if (request.nickname() != null) {
            profile.setNickname(request.nickname().trim());
        }

        if (request.bio() != null) {
            profile.setBio(request.bio().trim());
        }

        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl().trim().isEmpty() ? null : request.avatarUrl().trim());
        }

        if (request.bannerUrl() != null) {
            profile.setBannerUrl(request.bannerUrl().trim().isEmpty() ? null : request.bannerUrl().trim());
        }

        if (request.country() != null) {
            profile.setCountry(request.country().trim());
        }

        if (request.state() != null) {
            profile.setState(request.state().trim());
        }

        if (request.city() != null) {
            profile.setCity(request.city().trim());
        }

        if (request.mainRole() != null) {
            profile.setMainRole(request.mainRole());
        }

        if (request.secondaryRole() != null) {
            profile.setSecondaryRole(request.secondaryRole());
        }

        if (request.premierRating() != null) {
            profile.setPremierRating(request.premierRating());
        }

        if (request.faceitLevel() != null) {
            profile.setFaceitLevel(request.faceitLevel());
        }

        if (request.gcRank() != null) {
            profile.setGcRank(request.gcRank());
        }

        if (request.playstyle() != null) {
            profile.setPlaystyle(request.playstyle());
        }

        if (request.lookingForTeam() != null) {
            profile.setLookingForTeam(request.lookingForTeam());
        }

        if (request.lookingForDuo() != null) {
            profile.setLookingForDuo(request.lookingForDuo());
        }

        profile.setUpdatedAt(Instant.now());
    }

    public static GamerProfileResponse toResponse(GamerProfile savedProfile, List<GamerProfileFavoriteMap> favoriteMaps) {
        List<Cs2Map> maps = favoriteMaps.stream()
                .map(GamerProfileFavoriteMap::getMapName)
                .toList();

        return new GamerProfileResponse(
                savedProfile.getId(),
                savedProfile.getUser().getId(),
                savedProfile.getUser().getUsername(),
                savedProfile.getNickname(),
                savedProfile.getBio(),
                savedProfile.getAvatarUrl(),
                savedProfile.getBannerUrl(),
                savedProfile.getCountry(),
                savedProfile.getCity(),
                savedProfile.getState(),
                savedProfile.getMainRole(),
                savedProfile.getSecondaryRole(),
                savedProfile.getPremierRating(),
                savedProfile.getFaceitLevel(),
                savedProfile.getGcRank(),
                savedProfile.getPlaystyle(),
                savedProfile.getLookingForTeam(),
                savedProfile.getLookingForDuo(),
                savedProfile.getSetupStatus(),
                maps,
                savedProfile.getCreatedAt(),
                savedProfile.getUpdatedAt()
        );
    }

    public static GamerProfile toEntity(User user) {
        return toEntity(user, user.getUsername(), null, null);
    }

    public static GamerProfile toEntity(User user, String nickname, String avatarUrl, String country) {
        Instant now = Instant.now();
        return GamerProfile.builder()
                .user(user)
                .nickname((nickname != null && !nickname.isBlank()) ? nickname.trim() : user.getUsername())
                .avatarUrl(avatarUrl)
                .country(country)
                .lookingForDuo(false)
                .lookingForTeam(false)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static boolean updateFromSteam(GamerProfile profile, String personaName, String avatarUrl, String country) {
        boolean changed = false;
        if (avatarUrl != null && !avatarUrl.isBlank() && (profile.getAvatarUrl() == null || profile.getAvatarUrl().isBlank())) {
            profile.setAvatarUrl(avatarUrl.trim());
            changed = true;
        }
        if (personaName != null && !personaName.isBlank() && profile.getNickname() != null && profile.getNickname().startsWith("steam_")) {
            profile.setNickname(personaName.trim());
            changed = true;
        }
        if (country != null && !country.isBlank() && (profile.getCountry() == null || profile.getCountry().isBlank())) {
            profile.setCountry(country.trim());
            changed = true;
        }
        return changed;
    }

    public static GamerProfileFavoriteMap toFavoriteMapEntity(GamerProfile profile, Cs2Map map) {
        return GamerProfileFavoriteMap.builder()
                .profile(profile)
                .mapName(map)
                .createdAt(Instant.now())
                .build();
    }
}
