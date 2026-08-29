package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.steam.SteamPlayerSummary;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SteamProfileSyncServiceTest {

    @Mock
    private GamerProfileRepository gamerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SteamProfileSyncService syncService;

    @Test
    void syncIfMissing_whenSummaryOrUserIsNull_shouldDoNothing() {
        syncService.syncIfMissing(null, null);
        verifyNoInteractions(gamerProfileRepository, userRepository);
    }

    @Test
    void syncIfMissing_whenProfileHasFallbackFields_shouldUpdateProfileAndUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("steam_123456")
                .build();

        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .nickname("steam_123456")
                .avatarUrl(null)
                .country(null)
                .build();

        SteamPlayerSummary summary = new SteamPlayerSummary(
                "76561198012345678",
                "ProPlayer",
                "https://steamcommunity.com/id/proplayer",
                "https://avatar.url",
                "BR"
        );

        when(gamerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        syncService.syncIfMissing(user, summary);

        assertThat(profile.getNickname()).isEqualTo("ProPlayer");
        assertThat(profile.getAvatarUrl()).isEqualTo("https://avatar.url");
        assertThat(profile.getCountry()).isEqualTo("BR");
        assertThat(user.getFirstName()).isEqualTo("ProPlayer");

        verify(gamerProfileRepository).save(profile);
        verify(userRepository).save(user);
    }

    @Test
    void syncIfMissing_whenProfileAlreadyFilled_shouldNotSave() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("CustomName")
                .build();

        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .nickname("CustomNick")
                .avatarUrl("https://existing.avatar")
                .country("US")
                .build();

        SteamPlayerSummary summary = new SteamPlayerSummary(
                "76561198012345678",
                "SteamName",
                "https://steamcommunity.com/id/proplayer",
                "https://avatar.url",
                "BR"
        );

        when(gamerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        syncService.syncIfMissing(user, summary);

        verify(gamerProfileRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}
