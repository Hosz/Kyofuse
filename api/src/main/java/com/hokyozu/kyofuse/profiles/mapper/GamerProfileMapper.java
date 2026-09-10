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
            profile.setNickname(sanitizeHtml(request.nickname()));
        }

        if (request.bio() != null) {
            profile.setBio(sanitizeHtml(request.bio()));
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

        if (request.showCountryFlag() != null) {
            profile.setShowCountryFlag(request.showCountryFlag());
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

        User user = savedProfile.getUser();
        String regCountry = user != null ? user.getRegistrationCountry() : null;
        String regCountryCode = user != null ? user.getRegistrationCountryCode() : null;
        String regDevice = user != null ? user.getRegistrationDevice() : null;

        if (regCountry == null && savedProfile.getCountry() != null && !savedProfile.getCountry().isBlank()) {
            regCountry = savedProfile.getCountry();
        }

        return new GamerProfileResponse(
                savedProfile.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                savedProfile.getNickname(),
                savedProfile.getBio(),
                savedProfile.getAvatarUrl(),
                savedProfile.getBannerUrl(),
                savedProfile.getCountry(),
                savedProfile.getCity(),
                savedProfile.getState(),
                savedProfile.getShowCountryFlag() != null ? savedProfile.getShowCountryFlag() : true,
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
                savedProfile.getUpdatedAt(),
                regCountry,
                regCountryCode,
                regDevice
        );
    }

    public static GamerProfile toEntity(User user) {
        return toEntity(user, user.getUsername(), null, null);
    }

    public static GamerProfile toEntity(User user, String nickname, String avatarUrl, String country) {
        Instant now = Instant.now();
        String safeNickname = (nickname != null && !nickname.isBlank()) ? sanitizeHtml(nickname) : user.getUsername();
        return GamerProfile.builder()
                .user(user)
                .nickname(safeNickname)
                .avatarUrl(avatarUrl)
                .country(country)
                .showCountryFlag(true)
                .lookingForDuo(false)
                .lookingForTeam(false)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static String sanitizeHtml(String text) {
        if (text == null) {
            return null;
        }
        String withoutScripts = text.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", "");
        return withoutScripts.replaceAll("<[^>]*>", "").trim();
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
