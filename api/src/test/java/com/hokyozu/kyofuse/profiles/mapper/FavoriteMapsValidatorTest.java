package com.hokyozu.kyofuse.profiles.mapper;

import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FavoriteMapsValidatorTest {

    private final FavoriteMapsValidator validator = new FavoriteMapsValidator();

    @Test
    void validatePassesWithUpToThreeUniqueMaps() {
        assertThatCode(() -> validator.validate(List.of(Cs2Map.MIRAGE, Cs2Map.INFERNO, Cs2Map.NUKE)))
                .doesNotThrowAnyException();
    }

    @Test
    void validateThrowsWithMoreThanThreeMaps() {
        assertThatThrownBy(() -> validator.validate(List.of(
                Cs2Map.MIRAGE,
                Cs2Map.INFERNO,
                Cs2Map.NUKE,
                Cs2Map.TRAIN
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Você pode escolher no máximo 3 mapas favoritos.");
    }

    @Test
    void validateThrowsWithDuplicateMaps() {
        assertThatThrownBy(() -> validator.validate(List.of(Cs2Map.MIRAGE, Cs2Map.MIRAGE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Não é permitido repetir mapas favoritos.");
    }
}
