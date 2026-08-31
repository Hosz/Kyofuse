package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileFavoriteMapRepository;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.profiles.dto.request.ProfileFilter;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GamerProfileServiceTest {

    @Mock
    private GamerProfileRepository gamerProfileRepository;

    @Mock
    private GamerProfileFavoriteMapRepository favoriteMapRepository;

    @Mock
    private GamerProfileSetupStatusResolverService setupStatusResolverService;

    @Mock
    private UpdateFavoriteMapsService updateFavoriteMapsService;

    @Mock
    private GamerProfileFinder gamerProfileFinder;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ProfilePermissionService profilePermissionService;

    @Mock
    private com.hokyozu.kyofuse.storage.service.ImageProcessingService imageProcessingService;

    @InjectMocks
    private GamerProfileService service;

    @Test
    void createGamerProfileMinSavesMinimalPendingProfile() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("player")
                .build();

        service.createGamerProfileMin(user);

        ArgumentCaptor<GamerProfile> captor = ArgumentCaptor.forClass(GamerProfile.class);
        verify(gamerProfileRepository).save(captor.capture());

        GamerProfile profile = captor.getValue();
        assertThat(profile.getUser()).isSameAs(user);
        assertThat(profile.getNickname()).isEqualTo("player");
        assertThat(profile.getLookingForDuo()).isFalse();
        assertThat(profile.getLookingForTeam()).isFalse();
        assertThat(profile.getSetupStatus()).isEqualTo(GamerProfileSetupStatus.PENDING);
        assertThat(profile.getCreatedAt()).isNotNull();
        assertThat(profile.getUpdatedAt()).isNotNull();
    }

    @Test
    void editProfileUpdatesProfileFavoriteMapsAndSetupStatus() {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        User user = User.builder().id(userId).username("old").build();
        GamerProfile profile = GamerProfile.builder()
                .id(profileId)
                .user(user)
                .nickname("old")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        GamerProfileRequest request = new GamerProfileRequest(
                " newNick ",
                " new bio ",
                " avatar ",
                " banner ",
                " BR ",
                " Sao Paulo ",
                " SP ",
                PlayerRole.AWPER,
                PlayerRole.RIFLER,
                15000,
                8,
                18,
                Playstyle.COMPETITIVE,
                true,
                false,
                List.of(Cs2Map.MIRAGE)
        );
        GamerProfileFavoriteMap favoriteMap = GamerProfileFavoriteMap.builder()
                .profile(profile)
                .mapName(Cs2Map.MIRAGE)
                .createdAt(Instant.now())
                .build();

        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(setupStatusResolverService.resolve(profile)).thenReturn(GamerProfileSetupStatus.COMPLETED);
        when(gamerProfileRepository.save(profile)).thenReturn(profile);
        when(favoriteMapRepository.findByProfile_Id(profileId)).thenReturn(List.of(favoriteMap));

        GamerProfileResponse response = service.editProfile(userId, request);

        verify(updateFavoriteMapsService).execute(profile, request.favoriteMaps());
        assertThat(profile.getNickname()).isEqualTo("newNick");
        assertThat(profile.getBio()).isEqualTo("new bio");
        assertThat(profile.getSetupStatus()).isEqualTo(GamerProfileSetupStatus.COMPLETED);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.favoriteMaps()).containsExactly(Cs2Map.MIRAGE);
    }

    @Test
    void editProfileThrowsWhenNicknameIsBlank() {
        GamerProfileRequest request = new GamerProfileRequest(
                "   ",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> service.editProfile(UUID.randomUUID(), request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Nickname cannot be empty");
    }

    @Test
    void editProfileThrowsWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(gamerProfileFinder.findProfileByUserId(userId))
                .thenThrow(new RuntimeException("Gamer profile not found for user ID: " + userId));

        assertThatThrownBy(() -> service.editProfile(userId, emptyRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for user ID: " + userId);
    }

    @Test
    void editProfileThrowsWhenMainRoleAndSecondaryRoleAreIdentical() {
        UUID userId = UUID.randomUUID();
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .mainRole(PlayerRole.AWPER)
                .secondaryRole(null)
                .build();
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);

        GamerProfileRequest request = new GamerProfileRequest(
                "nickname",
                null,
                null,
                null,
                null,
                null,
                null,
                PlayerRole.AWPER,
                PlayerRole.AWPER,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> service.editProfile(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Main role and secondary role cannot be the same");
    }

    @Test
    void editProfileUnlinksSecondaryRoleWhenSecondaryRoleNull() {
        UUID userId = UUID.randomUUID();
        GamerProfile profile = profile(userId);
        profile.setMainRole(PlayerRole.AWPER);
        profile.setSecondaryRole(PlayerRole.RIFLER);

        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(gamerProfileRepository.save(profile)).thenReturn(profile);

        GamerProfileRequest request = new GamerProfileRequest(
                "nickname",
                null,
                null,
                null,
                null,
                null,
                null,
                PlayerRole.AWPER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        service.editProfile(userId, request);

        assertThat(profile.getMainRole()).isEqualTo(PlayerRole.AWPER);
        assertThat(profile.getSecondaryRole()).isNull();
    }

    @Test
    void editProfilePromotesSecondaryRoleToMainWhenMainRoleIsRemoved() {
        UUID userId = UUID.randomUUID();
        GamerProfile profile = profile(userId);
        profile.setMainRole(PlayerRole.AWPER);
        profile.setSecondaryRole(PlayerRole.RIFLER);

        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(gamerProfileRepository.save(profile)).thenReturn(profile);

        GamerProfileRequest request = new GamerProfileRequest(
                "nickname",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PlayerRole.RIFLER,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        service.editProfile(userId, request);

        assertThat(profile.getMainRole()).isEqualTo(PlayerRole.RIFLER);
        assertThat(profile.getSecondaryRole()).isNull();
    }

    @Test
    void editProfileClearsAllRolesWhenMainRoleRemovedAndNoSecondaryRole() {
        UUID userId = UUID.randomUUID();
        GamerProfile profile = profile(userId);
        profile.setMainRole(PlayerRole.AWPER);
        profile.setSecondaryRole(null);

        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(gamerProfileRepository.save(profile)).thenReturn(profile);

        GamerProfileRequest request = new GamerProfileRequest(
                "nickname",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        service.editProfile(userId, request);

        assertThat(profile.getMainRole()).isNull();
        assertThat(profile.getSecondaryRole()).isNull();
    }

    @Test
    void viewMyProfileReturnsProfileResponse() {
        UUID userId = UUID.randomUUID();
        GamerProfile profile = profile(userId);
        GamerProfileFavoriteMap favoriteMap = GamerProfileFavoriteMap.builder()
                .profile(profile)
                .mapName(Cs2Map.INFERNO)
                .createdAt(Instant.now())
                .build();
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
        when(favoriteMapRepository.findByProfile_Id(profile.getId())).thenReturn(List.of(favoriteMap));

        GamerProfileResponse response = service.viewMyProfile(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.favoriteMaps()).containsExactly(Cs2Map.INFERNO);
    }

    @Test
    void viewMyProfileThrowsWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(gamerProfileFinder.findProfileByUserId(userId))
                .thenThrow(new RuntimeException("Gamer profile not found for user ID: " + userId));

        assertThatThrownBy(() -> service.viewMyProfile(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for user ID: " + userId);
    }

    @Test
    void viewUserProfileReturnsProfileResponse() {
        UUID targetUserId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();
        String username = "player";
        User targetUser = User.builder().id(targetUserId).username(username).build();
        User viewer = User.builder().id(viewerId).username("viewer").build();
        GamerProfile profile = profile(targetUserId);
        when(userFinder.findProfileByUsername(username)).thenReturn(targetUser);
        when(userFinder.findProfileByUserId(viewerId)).thenReturn(viewer);
        when(gamerProfileFinder.findProfileByUserUsername(username)).thenReturn(profile);
        when(favoriteMapRepository.findByProfile_Id(profile.getId())).thenReturn(List.of());

        GamerProfileResponse response = service.viewUserProfile(username, viewerId);

        verify(profilePermissionService).validateViewProfile(viewer, targetUser);
        assertThat(response.id()).isEqualTo(profile.getId());
        assertThat(response.userId()).isEqualTo(targetUserId);
    }

    @Test
    void viewUserProfileThrowsWhenProfileDoesNotExist() {
        UUID viewerId = UUID.randomUUID();
        String username = "player";
        when(userFinder.findProfileByUsername(username))
                .thenThrow(new RuntimeException("User not found for username: " + username));

        assertThatThrownBy(() -> service.viewUserProfile(username, viewerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found for username: " + username);
    }

    @Test
    void listingProfilesReturnsPagedProfiles() {
        ProfileFilter filter = new ProfileFilter("player", null);
        Pageable pageable = PageRequest.of(0, 10);
        UUID userId = UUID.randomUUID();
        GamerProfile profile = profile(userId);
        Page<GamerProfile> page = new PageImpl<>(List.of(profile), pageable, 1);

        when(gamerProfileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(favoriteMapRepository.findByProfile_IdIn(List.of(profile.getId()))).thenReturn(List.of());

        Page<GamerProfileResponse> result = service.listingProfiles(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).username()).isEqualTo("player");
    }

    @Test
    void listingProfilesThrowsWhenStatusIsInvalid() {
        ProfileFilter filter = new ProfileFilter("player", UserStatus.INACTIVE);
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> service.listingProfiles(filter, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Filtro de status inválido.");
    }

    private static GamerProfileRequest emptyRequest() {
        return new GamerProfileRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static GamerProfile profile(UUID userId) {
        return GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(userId).username("player").build())
                .nickname("player")
                .lookingForDuo(false)
                .lookingForTeam(false)
                .setupStatus(GamerProfileSetupStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
