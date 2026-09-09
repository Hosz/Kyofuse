package com.hokyozu.kyofuse.infrastructure.geolocation;

import com.hokyozu.kyofuse.infrastructure.client.ClientIpResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;

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

    @Test
    void resolveLocationPrioritizesPreservedXClientHeadersOverOverwrittenCloudflareHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Client-Country", "BR");
        request.addHeader("X-Client-City", "Florianópolis");
        request.addHeader("X-Client-Region", "Santa Catarina");
        request.addHeader("CF-IPCountry", "US");
        request.addHeader("CF-IPCity", "Boardman");
        request.addHeader("CF-Region", "Oregon");
        when(clientIpResolver.cleanIp("177.18.29.40")).thenReturn("177.18.29.40");
        when(clientIpResolver.isLocalOrLoopback("177.18.29.40")).thenReturn(false);

        LocationInfo info = geoLocationService.resolveLocation(request, "177.18.29.40");

        assertThat(info.isLocal()).isFalse();
        assertThat(info.country()).isEqualTo("Brasil");
        assertThat(info.countryCode()).isEqualTo("BR");
        assertThat(info.city()).isEqualTo("Florianópolis");
        assertThat(info.state()).isEqualTo("Santa Catarina");
        assertThat(info.formattedLocation()).isEqualTo("Florianópolis, Santa Catarina, Brasil");
    }

    @Test
    void resolveLocationResolvesFromCloudflareHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-IPCountry", "BR");
        request.addHeader("CF-IPCity", "Curitiba");
        request.addHeader("CF-Region", "Paraná");
        when(clientIpResolver.cleanIp("200.189.1.5")).thenReturn("200.189.1.5");
        when(clientIpResolver.isLocalOrLoopback("200.189.1.5")).thenReturn(false);

        LocationInfo info = geoLocationService.resolveLocation(request, "200.189.1.5");

        assertThat(info.isLocal()).isFalse();
        assertThat(info.country()).isEqualTo("Brasil");
        assertThat(info.countryCode()).isEqualTo("BR");
        assertThat(info.city()).isEqualTo("Curitiba");
        assertThat(info.state()).isEqualTo("Paraná");
        assertThat(info.formattedLocation()).isEqualTo("Curitiba, Paraná, Brasil");
    }

    @Test
    void resolveLocationDecodesUrlEncodedCloudflareHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-IPCountry", "BR");
        request.addHeader("CF-IPCity", "S%C3%A3o%20Paulo");
        request.addHeader("CF-Region", "S%C3%A3o%20Paulo");
        when(clientIpResolver.cleanIp("200.189.1.5")).thenReturn("200.189.1.5");
        when(clientIpResolver.isLocalOrLoopback("200.189.1.5")).thenReturn(false);

        LocationInfo info = geoLocationService.resolveLocation(request, "200.189.1.5");

        assertThat(info.country()).isEqualTo("Brasil");
        assertThat(info.city()).isEqualTo("São Paulo");
        assertThat(info.state()).isEqualTo("São Paulo");
        assertThat(info.formattedLocation()).isEqualTo("São Paulo, Brasil");
    }

    @Test
    void resolveLocationRecognizesCloudflareTorNetwork() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-IPCountry", "T1");
        when(clientIpResolver.cleanIp("200.189.1.5")).thenReturn("200.189.1.5");
        when(clientIpResolver.isLocalOrLoopback("200.189.1.5")).thenReturn(false);

        LocationInfo info = geoLocationService.resolveLocation(request, "200.189.1.5");

        assertThat(info.country()).isEqualTo("Rede Tor");
        assertThat(info.countryCode()).isEqualTo("T1");
        assertThat(info.formattedLocation()).isEqualTo("Rede Tor");
    }

    @Test
    void resolveLocationExtractsFromRequestContextHolderWhenAvailable() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-IPCountry", "US");
        request.addHeader("CF-IPCity", "Miami");
        request.addHeader("CF-Region", "Florida");
        when(clientIpResolver.cleanIp("104.16.0.1")).thenReturn("104.16.0.1");
        when(clientIpResolver.isLocalOrLoopback("104.16.0.1")).thenReturn(false);

        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request)
        );

        try {
            LocationInfo info = geoLocationService.resolveLocation("104.16.0.1");
            assertThat(info.country()).isEqualTo("Estados Unidos");
            assertThat(info.city()).isEqualTo("Miami");
            assertThat(info.state()).isEqualTo("Florida");
            assertThat(info.formattedLocation()).isEqualTo("Miami, Florida, Estados Unidos");
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }
    }
}
