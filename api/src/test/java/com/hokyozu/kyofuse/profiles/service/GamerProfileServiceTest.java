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
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        User targetUser = User.builder().id(targetUserId).username("player").build();
        User viewer = User.builder().id(viewerId).username("viewer").build();
        GamerProfile profile = profile(targetUserId);
        when(userFinder.findProfileByUserId(targetUserId)).thenReturn(targetUser);
        when(userFinder.findProfileByUserId(viewerId)).thenReturn(viewer);
        when(gamerProfileFinder.findProfileByUserId(targetUserId)).thenReturn(profile);
        when(favoriteMapRepository.findByProfile_Id(profile.getId())).thenReturn(List.of());

        GamerProfileResponse response = service.viewUserProfile(targetUserId, viewerId);

        verify(profilePermissionService).validateViewProfile(viewer, targetUser);
        assertThat(response.id()).isEqualTo(profile.getId());
        assertThat(response.userId()).isEqualTo(targetUserId);
    }

    @Test
    void viewUserProfileThrowsWhenProfileDoesNotExist() {
        UUID targetUserId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(targetUserId))
                .thenThrow(new RuntimeException("Gamer profile not found for profile ID: " + targetUserId));

        assertThatThrownBy(() -> service.viewUserProfile(targetUserId, viewerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for profile ID: " + targetUserId);
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
