package com.hokyozu.kyofuse.infrastructure.security.steam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SteamService {

    private static final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/login";
    private static final String STEAM_API_PLAYER_SUMMARIES_URL = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";
    private static final Pattern STEAM_ID_PATTERN = Pattern.compile("https?://steamcommunity\\.com/openid/id/(\\d+)");

    private final String apiKey;
    private final String frontendUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public SteamService(
            @Value("${security.steam.api-key:}") String apiKey,
            @Value("${app.frontend.url:http://localhost:4200}") String frontendUrl
    ) {
        this(apiKey, frontendUrl, new ObjectMapper(), HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    public SteamService(
            String apiKey,
            String frontendUrl,
            ObjectMapper objectMapper,
            HttpClient httpClient
    ) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.frontendUrl = frontendUrl != null ? frontendUrl.trim() : "http://localhost:4200";
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String buildLoginUrl(String returnToUrl) {
        String targetReturnTo = (returnToUrl != null && !returnToUrl.isBlank())
                ? returnToUrl
                : (frontendUrl + "/auth/steam/callback");

        String realm = extractRealm(targetReturnTo);

        return STEAM_OPENID_URL + "?"
                + "openid.ns=" + urlEncode("http://specs.openid.net/auth/2.0")
                + "&openid.mode=checkid_setup"
                + "&openid.return_to=" + urlEncode(targetReturnTo)
                + "&openid.realm=" + urlEncode(realm)
                + "&openid.identity=" + urlEncode("http://specs.openid.net/auth/2.0/identifier_select")
                + "&openid.claimed_id=" + urlEncode("http://specs.openid.net/auth/2.0/identifier_select");
    }

    public String validateOpenIdAndGetSteamId(Map<String, String> openIdParams) {
        if (openIdParams == null || openIdParams.isEmpty()) {
            throw new UnauthorizedException("Parâmetros de autenticação OpenID da Steam ausentes.");
        }

        Map<String, String> normalizedParams = normalizeParams(openIdParams);

        String mode = normalizedParams.get("openid.mode");
        if ("cancel".equalsIgnoreCase(mode) || "error".equalsIgnoreCase(mode)) {
            throw new UnauthorizedException("Autenticação com a Steam cancelada ou recusada.");
        }

        if (!"id_res".equals(mode)) {
            throw new UnauthorizedException("Modo de resposta OpenID da Steam inválido: " + mode);
        }

        String claimedId = normalizedParams.get("openid.claimed_id");
        if (claimedId == null || claimedId.isBlank()) {
            throw new UnauthorizedException("Identificador OpenID (claimed_id) não encontrado.");
        }

        // Valida a assinatura diretamente com a Steam
        normalizedParams.put("openid.mode", "check_authentication");

        StringBuilder bodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : normalizedParams.entrySet()) {
            if (entry.getValue() != null) {
                if (!bodyBuilder.isEmpty()) {
                    bodyBuilder.append("&");
                }
                bodyBuilder.append(urlEncode(entry.getKey()))
                        .append("=")
                        .append(urlEncode(entry.getValue()));
            }
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(STEAM_OPENID_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(bodyBuilder.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200 || !response.body().contains("is_valid:true")) {
                log.warn("[SteamAuth] Validação OpenID falhou na Steam. Status: {}, Corpo: {}", response.statusCode(), response.body());
                throw new UnauthorizedException("Assinatura OpenID da Steam inválida ou expirada.");
            }
        } catch (UnauthorizedException ue) {
            throw ue;
        } catch (Exception e) {
            log.error("[SteamAuth] Erro ao contatar servidor OpenID da Steam: {}", e.getMessage(), e);
            throw new UnauthorizedException("Falha ao comunicar com o servidor da Steam: " + e.getMessage());
        }

        Matcher matcher = STEAM_ID_PATTERN.matcher(claimedId);
        if (!matcher.find()) {
            throw new UnauthorizedException("Não foi possível extrair o SteamID do claimed_id: " + claimedId);
        }

        return matcher.group(1);
    }

    public Optional<SteamPlayerSummary> getPlayerSummary(String steamId) {
        if (apiKey.isBlank()) {
            log.info("[SteamAuth] STEAM_API_KEY não configurada. Prosseguindo sem buscar dados adicionais do perfil.");
            return Optional.empty();
        }

        try {
            String url = STEAM_API_PLAYER_SUMMARIES_URL + "?key=" + urlEncode(apiKey) + "&steamids=" + urlEncode(steamId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                log.warn("[SteamAuth] Falha ao consultar Steam Web API. Status: {}", response.statusCode());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode players = root.path("response").path("players");
            if (players.isArray() && !players.isEmpty()) {
                JsonNode player = players.get(0);
                return Optional.of(new SteamPlayerSummary(
                        steamId,
                        player.path("personaname").asText(null),
                        player.path("profileurl").asText(null),
                        player.path("avatarfull").asText(null),
                        player.path("loccountrycode").asText(null)
                ));
            }
        } catch (Exception e) {
            log.warn("[SteamAuth] Erro ao buscar resumo do jogador Steam {}: {}", steamId, e.getMessage());
        }

        return Optional.empty();
    }

    private Map<String, String> normalizeParams(Map<String, String> original) {
        Map<String, String> normalized = new HashMap<>();
        for (Map.Entry<String, String> entry : original.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("openid.")) {
                key = "openid." + key;
            }
            normalized.put(key, entry.getValue());
        }
        return normalized;
    }

    private String extractRealm(String returnToUrl) {
        try {
            URI uri = URI.create(returnToUrl);
            String port = (uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443)
                    ? ":" + uri.getPort()
                    : "";
            return uri.getScheme() + "://" + uri.getHost() + port + "/";
        } catch (Exception e) {
            return frontendUrl.endsWith("/") ? frontendUrl : (frontendUrl + "/");
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
