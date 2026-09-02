package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.mapper.FavoriteMapsValidator;
import com.hokyozu.kyofuse.profiles.mapper.GamerProfileMapper;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileFavoriteMapRepository;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateFavoriteMapsService {

    private final GamerProfileFavoriteMapRepository gamerProfileFavoriteMapRepository;

    private final FavoriteMapsValidator favoriteMapsValidator;

    @Transactional
    public void execute(GamerProfile profile, List<Cs2Map> favoriteMaps) {
        if (favoriteMaps == null) {
            return;
        }

        favoriteMapsValidator.validate(favoriteMaps);

        gamerProfileFavoriteMapRepository.deleteByProfile_Id(profile.getId());
        // Num mesmo flush o Hibernate executa todos os INSERTs antes dos DELETEs. Sem
        // forçar a remoção agora, regravar um mapa que já era favorito tentaria inserir
        // a linha antes de a antiga sair e violaria uk_gamer_profile_favorite_maps_profile_map.
        gamerProfileFavoriteMapRepository.flush();

        List<GamerProfileFavoriteMap> maps = favoriteMaps.stream()
                .distinct()
                .map(map -> GamerProfileMapper.toFavoriteMapEntity(profile, map))
                .toList();

        gamerProfileFavoriteMapRepository.saveAll(maps);
    }
}
