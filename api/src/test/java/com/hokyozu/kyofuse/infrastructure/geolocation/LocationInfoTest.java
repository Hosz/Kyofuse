package com.hokyozu.kyofuse.infrastructure.geolocation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationInfoTest {

    @Test
    void localReturnsLocalDetails() {
        LocationInfo local = LocationInfo.local("127.0.0.1");

        assertThat(local.ip()).isEqualTo("127.0.0.1");
        assertThat(local.isLocal()).isTrue();
        assertThat(local.formattedLocation()).isEqualTo("Rede Local (Desenvolvimento)");
    }

    @Test
    void unknownReturnsUnknownDetails() {
        LocationInfo unknown = LocationInfo.unknown("203.0.113.1");

        assertThat(unknown.ip()).isEqualTo("203.0.113.1");
        assertThat(unknown.isLocal()).isFalse();
        assertThat(unknown.formattedLocation()).isEqualTo("Localização não identificada");
    }

    @Test
    void ofFormatsDistinctCityStateCountry() {
        LocationInfo location = LocationInfo.of("200.189.1.5", "Campinas", "São Paulo", "Brasil", "BR");

        assertThat(location.city()).isEqualTo("Campinas");
        assertThat(location.state()).isEqualTo("São Paulo");
        assertThat(location.country()).isEqualTo("Brasil");
        assertThat(location.countryCode()).isEqualTo("BR");
        assertThat(location.formattedLocation()).isEqualTo("Campinas, São Paulo, Brasil");
    }

    @Test
    void ofOmitsDuplicateStateWhenCityEqualsState() {
        LocationInfo location = LocationInfo.of("200.189.1.5", "São Paulo", "São Paulo", "Brasil", "BR");

        assertThat(location.formattedLocation()).isEqualTo("São Paulo, Brasil");
    }

    @Test
    void ofHandlesOnlyCountry() {
        LocationInfo location = LocationInfo.of("200.189.1.5", null, null, "Brasil", "BR");

        assertThat(location.formattedLocation()).isEqualTo("Brasil");
    }
}
