package com.hokyozu.kyofuse.infrastructure.security.steam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SteamServiceTest {

    @Mock
    private HttpClient httpClient;

    private SteamService steamService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        steamService = new SteamService("TEST_API_KEY", "http://localhost:4200", objectMapper, httpClient);
    }

    @Test
    void buildLoginUrlGeneratesValidSteamOpenIdUrl() {
        String loginUrl = steamService.buildLoginUrl("http://localhost:4200/auth/steam/callback");

        assertThat(loginUrl).startsWith("https://steamcommunity.com/openid/login?");
        assertThat(loginUrl).contains("openid.mode=checkid_setup");
        assertThat(loginUrl).contains("openid.return_to=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fsteam%2Fcallback");
        assertThat(loginUrl).contains("openid.realm=http%3A%2F%2Flocalhost%3A4200%2F");
    }

    @Test
    void validateOpenIdThrowsWhenParamsAreEmpty() {
        assertThatThrownBy(() -> steamService.validateOpenIdAndGetSteamId(Map.of()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Parâmetros de autenticação OpenID da Steam ausentes.");
    }

    @Test
    void validateOpenIdThrowsWhenModeIsCancel() {
        Map<String, String> params = Map.of("openid.mode", "cancel");
        assertThatThrownBy(() -> steamService.validateOpenIdAndGetSteamId(params))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Autenticação com a Steam cancelada ou recusada.");
    }

    @Test
    @SuppressWarnings("unchecked")
    void validateOpenIdExtractsSteamIdWhenSteamConfirmsValidity() throws Exception {
        Map<String, String> params = Map.of(
                "openid.mode", "id_res",
                "openid.claimed_id", "https://steamcommunity.com/openid/id/76561198012345678",
                "openid.sig", "dummy-signature"
        );

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("ns:http://specs.openid.net/auth/2.0\nis_valid:true\n");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String steamId = steamService.validateOpenIdAndGetSteamId(params);

        assertThat(steamId).isEqualTo("76561198012345678");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPlayerSummaryReturnsParsedDataWhenApiKeyPresent() throws Exception {
        String json = """
                {
                  "response": {
                    "players": [
                      {
                        "steamid": "76561198012345678",
                        "personaname": "GamerHero",
                        "profileurl": "https://steamcommunity.com/id/gamerhero/",
                        "avatarfull": "https://avatars.steamstatic.com/hero_full.jpg",
                        "loccountrycode": "BR"
                      }
                    ]
                  }
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Optional<SteamPlayerSummary> summaryOpt = steamService.getPlayerSummary("76561198012345678");

        assertThat(summaryOpt).isPresent();
        SteamPlayerSummary summary = summaryOpt.get();
        assertThat(summary.steamId()).isEqualTo("76561198012345678");
        assertThat(summary.personaName()).isEqualTo("GamerHero");
        assertThat(summary.avatarFull()).isEqualTo("https://avatars.steamstatic.com/hero_full.jpg");
        assertThat(summary.locCountryCode()).isEqualTo("BR");
    }
}
