package com.hokyozu.kyofuse.profiles.mapper;

import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FavoriteMapsValidator {

    public void validate(List<Cs2Map> favoriteMaps) {
        if (favoriteMaps.size() > 3) {
            throw new IllegalArgumentException("Você pode escolher no máximo 3 mapas favoritos.");
        }

        long uniqueCount = favoriteMaps.stream()
                .distinct()
                .count();

        if (uniqueCount != favoriteMaps.size()) {
            throw new IllegalArgumentException("Não é permitido repetir mapas favoritos.");
        }
    }
}
