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
    void rejectsMissingProfileById() {
        UUID profileId = UUID.randomUUID();
        when(repository.findById(profileId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findProfileById(profileId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for profile ID: " + profileId);
    }
}
