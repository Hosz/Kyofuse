package com.hokyozu.kyofuse.profiles.finder;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GamerProfileFinderTest {

    @Mock
    private GamerProfileRepository repository;
    @InjectMocks
    private GamerProfileFinder finder;

    @Test
    void findsProfileByUserId() {
        UUID userId = UUID.randomUUID();
        GamerProfile profile = GamerProfile.builder().build();
        when(repository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThat(finder.findProfileByUserId(userId)).isSameAs(profile);
    }

    @Test
    void rejectsMissingProfileByUserId() {
        UUID userId = UUID.randomUUID();
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findProfileByUserId(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for user ID: " + userId);
    }

    @Test
    void findsProfileById() {
        UUID profileId = UUID.randomUUID();
        GamerProfile profile = GamerProfile.builder().id(profileId).build();
        when(repository.findById(profileId)).thenReturn(Optional.of(profile));

        assertThat(finder.findProfileById(profileId)).isSameAs(profile);
    }

    @Test
    void findsProfileByUserUsername() {
        String username = "player";
        GamerProfile profile = GamerProfile.builder().build();
        when(repository.findByUserUsername(username)).thenReturn(Optional.of(profile));

        assertThat(finder.findProfileByUserUsername(username)).isSameAs(profile);
    }

    @Test
    void rejectsMissingProfileByUserUsername() {
        String username = "player";
        when(repository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findProfileByUserUsername(username))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for username: " + username);
    }

    @Test
    void findsAllByUserIds() {
        UUID userId = UUID.randomUUID();
        GamerProfile profile = GamerProfile.builder().build();
        when(repository.findByUserIdIn(java.util.List.of(userId))).thenReturn(java.util.List.of(profile));

        assertThat(finder.findAllByUserIds(java.util.List.of(userId))).containsExactly(profile);
    }

    @Test
    void findsAllByUserIdsReturnsEmptyWhenEmptyList() {
        assertThat(finder.findAllByUserIds(java.util.List.of())).isEmpty();
    }

    @Test
    void findsFullProfileByUserId() {
        UUID userId = UUID.randomUUID();
        GamerProfile profile = GamerProfile.builder().build();
        when(repository.findFullByUserId(userId)).thenReturn(Optional.of(profile));

        assertThat(finder.findFullProfileByUserId(userId)).isSameAs(profile);
    }

    @Test
    void rejectsMissingFullProfileByUserId() {
        UUID userId = UUID.randomUUID();
        when(repository.findFullByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findFullProfileByUserId(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Full gamer profile not found for user ID: " + userId);
    }

    @Test
    void findsFullProfileByUserUsername() {
        String username = "player";
        GamerProfile profile = GamerProfile.builder().build();
        when(repository.findFullByUserUsername(username)).thenReturn(Optional.of(profile));

        assertThat(finder.findFullProfileByUserUsername(username)).isSameAs(profile);
    }

    @Test
    void rejectsMissingFullProfileByUserUsername() {
        String username = "player";
        when(repository.findFullByUserUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findFullProfileByUserUsername(username))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Full gamer profile not found for username: " + username);
    }
}
