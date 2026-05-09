package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.mapper.FavoriteMapsValidator;
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

        List<GamerProfileFavoriteMap> maps = favoriteMaps.stream()
                .distinct()
                .map(map -> GamerProfileFavoriteMap.builder()
                        .profile(profile)
                        .mapName(map)
                        .createdAt(Instant.now())
                        .build())
                .toList();

        gamerProfileFavoriteMapRepository.saveAll(maps);
    }
}
