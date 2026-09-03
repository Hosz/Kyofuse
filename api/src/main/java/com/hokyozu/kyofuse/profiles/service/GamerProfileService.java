package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.request.ProfileFilter;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.profiles.mapper.GamerProfileMapper;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileFavoriteMapRepository;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.profiles.specification.ProfileSpecification;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.teams.mapper.TeamMapper;
import com.hokyozu.kyofuse.teams.specification.TeamSpecification;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hokyozu.kyofuse.storage.service.ImageProcessingService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamerProfileService {

    private final GamerProfileRepository gamerProfileRepository;
    private final GamerProfileFavoriteMapRepository gamerProfileFavoriteMapRepository;

    private final GamerProfileSetupStatusResolverService setupStatusResolverService;
    private final UpdateFavoriteMapsService updateFavoriteMapsService;
    private final ProfilePermissionService profilePermissionService;
    private final ImageProcessingService imageProcessingService;
    private final GamerProfileFinder gamerProfileFinder;
    private final UserFinder userFinder;
    private final com.hokyozu.kyofuse.leaderboard.service.LeaderboardService leaderboardService;
    private final ProfileAnalyticsService profileAnalyticsService;

    @Caching(evict = {
            @CacheEvict(value = "user_profiles", key = "'id:' + #userId"),
            @CacheEvict(value = "user_profiles", allEntries = true)
    })
    @Transactional
    public GamerProfileResponse uploadAvatar(UUID userId, MultipartFile file) throws IOException {
        GamerProfile profile = gamerProfileFinder.findProfileByUserId(userId);
        String avatarUrl = imageProcessingService.processAndUploadAvatar(userId, "users", file);
        profile.setAvatarUrl(avatarUrl);
        profile.setSetupStatus(setupStatusResolverService.resolve(profile));
        gamerProfileRepository.save(profile);
        List<GamerProfileFavoriteMap> favoriteMaps =
                gamerProfileFavoriteMapRepository.findByProfile_Id(profile.getId());
        return GamerProfileMapper.toResponse(profile, favoriteMaps);
    }

    @Caching(evict = {
            @CacheEvict(value = "user_profiles", key = "'id:' + #userId"),
            @CacheEvict(value = "user_profiles", allEntries = true)
    })
    @Transactional
    public GamerProfileResponse uploadBanner(UUID userId, MultipartFile file) throws IOException {
        GamerProfile profile = gamerProfileFinder.findProfileByUserId(userId);
        String bannerUrl = imageProcessingService.processAndUploadBanner(userId, "users", file);
        profile.setBannerUrl(bannerUrl);
        gamerProfileRepository.save(profile);
        List<GamerProfileFavoriteMap> favoriteMaps =
                gamerProfileFavoriteMapRepository.findByProfile_Id(profile.getId());
        return GamerProfileMapper.toResponse(profile, favoriteMaps);
    }

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

    @Caching(evict = {
            @CacheEvict(value = "user_profiles", key = "'id:' + #userId"),
            @CacheEvict(value = "user_profiles", allEntries = true)
    })
    @Transactional
    public GamerProfileResponse editProfile(UUID userId, GamerProfileRequest request) {

        if (request.nickname() != null && request.nickname().trim().isEmpty()) {
            throw new UnauthorizedException("Nickname cannot be empty");
        }

        GamerProfile profile = gamerProfileFinder.findProfileByUserId(userId);

        PlayerRole newMain = request.mainRole();
        PlayerRole newSecondary = request.secondaryRole();

        if (newMain == null && newSecondary != null) {
            newMain = newSecondary;
            newSecondary = null;
        }

        if (newMain != null && newSecondary != null && newMain == newSecondary) {
            throw new BadRequestException("Main role and secondary role cannot be the same");
        }

        GamerProfileMapper.updateEntity(profile, request);
        profile.setMainRole(newMain);
        profile.setSecondaryRole(newSecondary);

        updateFavoriteMapsService.execute(profile, request.favoriteMaps());
        GamerProfileSetupStatus newStatus = setupStatusResolverService.resolve(profile);
        if (newStatus == GamerProfileSetupStatus.PENDING) {
            newStatus = GamerProfileSetupStatus.PARTIAL;
        }
        profile.setSetupStatus(newStatus);

        GamerProfile savedProfile = gamerProfileRepository.save(profile);
        if (request.premierRating() != null) {
            leaderboardService.updateScore(userId, request.premierRating());
        }
        List<GamerProfileFavoriteMap> favoriteMaps =
                gamerProfileFavoriteMapRepository.findByProfile_Id(savedProfile.getId());
        return GamerProfileMapper.toResponse(savedProfile, favoriteMaps);
    }

    @Cacheable(value = "user_profiles", key = "'id:' + #userId")
    @Transactional(readOnly = true)
    public GamerProfileResponse viewMyProfile(UUID userId) {
        GamerProfile gamerProfile = gamerProfileFinder.findFullProfileByUserId(userId);

        return GamerProfileMapper.toResponse(gamerProfile, gamerProfile.getFavoriteMaps());
    }

    @Transactional(readOnly = true)
    public GamerProfileResponse viewUserProfile(String username, UUID userId) {
        User requestingUser = userFinder.findProfileByUsername(username);
        User user = userFinder.findProfileByUserId(userId);

        if (userId.equals(requestingUser.getId())) {
            return viewMyProfile(userId);
        }

        profilePermissionService.validateViewProfile(user, requestingUser);

        GamerProfile userRequestedProfile = gamerProfileFinder.findFullProfileByUserUsername(username);
        profileAnalyticsService.recordProfileVisit(userRequestedProfile.getUser().getId(), userId);

        return GamerProfileMapper.toResponse(userRequestedProfile, userRequestedProfile.getFavoriteMaps());
    }

    @Transactional(readOnly = true)
    public Page<GamerProfileResponse> listingProfiles(@Valid ProfileFilter filter, Pageable pageable) {
        if (filter.status() != null && filter.status() != UserStatus.ACTIVE) {
            throw new BadRequestException("Filtro de status inválido.");
        }

        Specification<GamerProfile> spec = Specification
                .where(ProfileSpecification.usernameContains(filter.username()))
                .and(ProfileSpecification.hasUserStatus(filter.status()));

        Page<GamerProfile> profiles = gamerProfileRepository.findAll(spec, pageable);
        List<UUID> profileIds = profiles.stream()
                .map(GamerProfile::getId)
                .toList();

        List<GamerProfileFavoriteMap> mapsByProfileId = profileIds.isEmpty()
                ? List.of()
                : gamerProfileFavoriteMapRepository.findByProfile_IdIn(profileIds);

        Map<UUID, List<GamerProfileFavoriteMap>> mapsByProfile = mapsByProfileId.stream()
                .collect(Collectors.groupingBy(map -> map.getProfile().getId()));

        return profiles.map(profile -> GamerProfileMapper.toResponse(
                profile,
                mapsByProfile.getOrDefault(profile.getId(), List.of())
        ));
    }
}
