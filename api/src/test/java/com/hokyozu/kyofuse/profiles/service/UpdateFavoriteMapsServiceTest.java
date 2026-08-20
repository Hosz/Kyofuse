package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.mapper.FavoriteMapsValidator;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileFavoriteMapRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateFavoriteMapsServiceTest {

    @Mock
    private GamerProfileFavoriteMapRepository favoriteMapRepository;

    @Mock
    private FavoriteMapsValidator favoriteMapsValidator;

    @InjectMocks
    private UpdateFavoriteMapsService service;

    @Test
    void executeDoesNothingWhenFavoriteMapsIsNull() {
        GamerProfile profile = GamerProfile.builder().id(UUID.randomUUID()).build();

        service.execute(profile, null);

        verify(favoriteMapsValidator, never()).validate(null);
        verify(favoriteMapRepository, never()).deleteByProfile_Id(profile.getId());
    }

    @Test
    void executeFlushesRemovalBeforeInsertingSoRekeepingAMapDoesNotBreakTheUniqueIndex() {
        // Regressão: o Hibernate executa INSERTs antes de DELETEs no mesmo flush, então
        // sem o flush explícito reenviar um mapa que já era favorito estourava
        // uk_gamer_profile_favorite_maps_profile_map.
        UUID profileId = UUID.randomUUID();
        GamerProfile profile = GamerProfile.builder().id(profileId).build();

        service.execute(profile, List.of(Cs2Map.MIRAGE));

        InOrder inOrder = inOrder(favoriteMapRepository);
        inOrder.verify(favoriteMapRepository).deleteByProfile_Id(profileId);
        inOrder.verify(favoriteMapRepository).flush();
        inOrder.verify(favoriteMapRepository).saveAll(anyList());
    }

    @Test
    void executeReplacesExistingMapsWithDistinctMaps() {
        UUID profileId = UUID.randomUUID();
        GamerProfile profile = GamerProfile.builder().id(profileId).build();
        List<Cs2Map> favoriteMaps = List.of(Cs2Map.MIRAGE, Cs2Map.INFERNO);

        service.execute(profile, favoriteMaps);

        verify(favoriteMapsValidator).validate(favoriteMaps);
        verify(favoriteMapRepository).deleteByProfile_Id(profileId);

        ArgumentCaptor<List<GamerProfileFavoriteMap>> captor = ArgumentCaptor.forClass(List.class);
        verify(favoriteMapRepository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .extracting(GamerProfileFavoriteMap::getMapName)
                .containsExactly(Cs2Map.MIRAGE, Cs2Map.INFERNO);
        assertThat(captor.getValue())
                .allSatisfy(map -> {
                    assertThat(map.getProfile()).isSameAs(profile);
                    assertThat(map.getCreatedAt()).isNotNull();
                });
    }
}
