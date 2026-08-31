package com.hokyozu.kyofuse.infrastructure.geolocation;

import com.hokyozu.kyofuse.infrastructure.client.ClientIpResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoLocationServiceTest {

    @Mock
    private ClientIpResolver clientIpResolver;

    @Mock
    private ResourceLoader resourceLoader;

    private GeoLocationService geoLocationService;

    @BeforeEach
    void setUp() {
        geoLocationService = new GeoLocationService(clientIpResolver, resourceLoader);
    }

    @Test
    void resolveLocationReturnsLocalForLoopbackOrPrivateIp() {
        when(clientIpResolver.cleanIp("127.0.0.1")).thenReturn("127.0.0.1");
        when(clientIpResolver.isLocalOrLoopback("127.0.0.1")).thenReturn(true);

        LocationInfo info = geoLocationService.resolveLocation("127.0.0.1");

        assertThat(info.isLocal()).isTrue();
        assertThat(info.formattedLocation()).isEqualTo("Rede Local (Desenvolvimento)");
    }

    @Test
    void resolveLocationReturnsUnknownWhenDatabaseNotLoaded() {
        when(clientIpResolver.cleanIp("203.0.113.195")).thenReturn("203.0.113.195");
        when(clientIpResolver.isLocalOrLoopback("203.0.113.195")).thenReturn(false);

        LocationInfo info = geoLocationService.resolveLocation("203.0.113.195");

        assertThat(info.isLocal()).isFalse();
        assertThat(info.formattedLocation()).isEqualTo("Localização não identificada");
    }

    @Test
    void resolveLocationHandlesNullOrBlankSafely() {
        LocationInfo info = geoLocationService.resolveLocation(null);
        assertThat(info.formattedLocation()).isEqualTo("Localização não identificada");

        LocationInfo blankInfo = geoLocationService.resolveLocation("   ");
        assertThat(blankInfo.formattedLocation()).isEqualTo("Localização não identificada");
    }
}
