package com.hokyozu.kyofuse.infrastructure.geolocation;

import com.hokyozu.kyofuse.infrastructure.client.ClientIpResolver;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoLocationService {

    private final ClientIpResolver clientIpResolver;
    private final ResourceLoader resourceLoader;

    @Value("${app.geoip.database-path:}")
    private String databasePath;

    private DatabaseReader databaseReader;

    @PostConstruct
    public void init() {
        if (databasePath == null || databasePath.isBlank()) {
            log.info("[GeoIP] Base de dados GeoLite2 não configurada (app.geoip.database-path vazia). Geolocalização operará primariamente via cabeçalhos Cloudflare.");
            return;
        }

        try {
            if (databasePath.startsWith("classpath:") || databasePath.startsWith("file:")) {
                Resource resource = resourceLoader.getResource(databasePath);
                if (resource.exists()) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        this.databaseReader = new DatabaseReader.Builder(inputStream)
                                .withCache(new CHMCache())
                                .build();
                        log.info("[GeoIP] Base de dados GeoLite2 carregada a partir do recurso: {}", databasePath);
                    }
                } else {
                    log.warn("[GeoIP] Recurso do banco GeoLite2 não encontrado: {}", databasePath);
                }
            } else {
                File databaseFile = new File(databasePath);
                if (databaseFile.exists() && databaseFile.canRead()) {
                    this.databaseReader = new DatabaseReader.Builder(databaseFile)
                            .withCache(new CHMCache())
                            .build();
                    log.info("[GeoIP] Base de dados GeoLite2 carregada a partir do arquivo: {}", databaseFile.getAbsolutePath());
                } else {
                    log.warn("[GeoIP] Arquivo do banco GeoLite2 não encontrado ou sem permissão de leitura: {}", databasePath);
                }
            }
        } catch (Exception e) {
            log.error("[GeoIP] Falha ao inicializar o leitor MaxMind GeoLite2: {}", e.getMessage(), e);
        }
    }

    public LocationInfo resolveLocation(String rawIp) {
        if (rawIp == null || rawIp.isBlank()) {
            return LocationInfo.unknown("desconhecido");
        }

        HttpServletRequest currentRequest = getCurrentHttpRequest();
        if (currentRequest != null) {
            return resolveLocation(currentRequest, rawIp);
        }

        return resolveFromIp(rawIp);
    }

    public LocationInfo resolveLocationFromRequest(HttpServletRequest request) {
        if (request == null) {
            return LocationInfo.unknown("desconhecido");
        }
        String clientIp = clientIpResolver.resolve(request);
        return resolveLocation(request, clientIp);
    }

    public LocationInfo resolveLocation(HttpServletRequest request, String rawIp) {
        String effectiveIp = rawIp;
        if (request != null && (effectiveIp == null || effectiveIp.isBlank() || clientIpResolver.isLocalOrLoopback(effectiveIp))) {
            String resolved = clientIpResolver.resolve(request);
            if (resolved != null && !resolved.isBlank() && !clientIpResolver.isLocalOrLoopback(resolved)) {
                effectiveIp = resolved;
            }
        }
        String cleanedIp = clientIpResolver.cleanIp(
                effectiveIp != null && !effectiveIp.isBlank() ? effectiveIp : (request != null ? clientIpResolver.resolve(request) : "desconhecido")
        );

        if (clientIpResolver.isLocalOrLoopback(cleanedIp)) {
            return LocationInfo.local(cleanedIp);
        }

        if (request != null) {
            LocationInfo cfLocation = resolveFromCloudflareHeaders(request, cleanedIp);
            if (cfLocation != null) {
                return cfLocation;
            }
        }

        return resolveFromIp(cleanedIp);
    }

    public LocationInfo resolveFromIp(String rawIp) {
        if (rawIp == null || rawIp.isBlank()) {
            return LocationInfo.unknown("desconhecido");
        }

        String cleanedIp = clientIpResolver.cleanIp(rawIp);
        if (clientIpResolver.isLocalOrLoopback(cleanedIp)) {
            return LocationInfo.local(cleanedIp);
        }

        return resolveFromDatabase(cleanedIp);
    }

    public LocationInfo resolveFromCloudflareHeaders(HttpServletRequest request, String cleanedIp) {
        if (request == null) {
            return null;
        }

        String rawCountry = decodeHeaderValue(firstHeader(request, "X-Client-Country", "CF-IPCountry"));
        String city = decodeHeaderValue(firstHeader(request, "X-Client-City", "CF-IPCity"));
        String state = decodeHeaderValue(firstHeader(request, "X-Client-Region", "CF-Region"));
        if (state == null) {
            state = decodeHeaderValue(firstHeader(request, "X-Client-Region-Code", "CF-Region-Code"));
        }

        if (rawCountry == null && city == null && state == null) {
            return null;
        }

        String countryCode = null;
        String country = null;

        if (rawCountry != null) {
            String normalizedCountry = rawCountry.toUpperCase(Locale.ROOT);
            if ("T1".equals(normalizedCountry)) {
                countryCode = "T1";
                country = "Rede Tor";
            } else if (!"XX".equals(normalizedCountry) && normalizedCountry.length() == 2) {
                countryCode = normalizedCountry;
                country = getCountryName(normalizedCountry);
            }
        }

        if (countryCode == null && city == null && state == null) {
            return null;
        }

        return LocationInfo.of(cleanedIp, city, state, country, countryCode);
    }

    private String firstHeader(HttpServletRequest request, String primaryHeader, String fallbackHeader) {
        String val = request.getHeader(primaryHeader);
        if (val != null && !val.isBlank()) {
            return val;
        }
        return request.getHeader(fallbackHeader);
    }

    public LocationInfo resolveFromDatabase(String cleanedIp) {
        if (databaseReader == null) {
            return LocationInfo.unknown(cleanedIp);
        }

        try {
            InetAddress ipAddress = InetAddress.getByName(cleanedIp);
            CityResponse response = databaseReader.city(ipAddress);

            String city = response.getCity() != null ? response.getCity().getName() : null;
            String state = response.getMostSpecificSubdivision() != null ? response.getMostSpecificSubdivision().getName() : null;
            String country = null;
            String countryCode = null;

            if (response.getCountry() != null) {
                if (response.getCountry().getNames() != null && response.getCountry().getNames().containsKey("pt-BR")) {
                    country = response.getCountry().getNames().get("pt-BR");
                } else {
                    country = response.getCountry().getName();
                }
                countryCode = response.getCountry().getIsoCode();
            }

            return LocationInfo.of(cleanedIp, city, state, country, countryCode);
        } catch (AddressNotFoundException e) {
            log.debug("[GeoIP] IP não encontrado na base GeoLite2: {}", cleanedIp);
            return LocationInfo.unknown(cleanedIp);
        } catch (Exception e) {
            log.warn("[GeoIP] Erro ao consultar localização para o IP {}: {}", cleanedIp, e.getMessage());
            return LocationInfo.unknown(cleanedIp);
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
                return servletRequestAttributes.getRequest();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String getCountryName(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }
        try {
            Locale ptBr = Locale.forLanguageTag("pt-BR");
            Locale locale = new Locale.Builder().setRegion(countryCode).build();
            String displayCountry = locale.getDisplayCountry(ptBr);
            if (displayCountry != null && !displayCountry.isBlank() && !displayCountry.equalsIgnoreCase(countryCode)) {
                return displayCountry;
            }
        } catch (Exception e) {
            log.debug("[GeoIP] Erro ao obter nome do país para o código {}: {}", countryCode, e.getMessage());
        }
        return countryCode;
    }

    private String decodeHeaderValue(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isBlank() || "unknown".equalsIgnoreCase(trimmed)) {
            return null;
        }
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.isBlank() || "unknown".equalsIgnoreCase(trimmed)) {
            return null;
        }
        if (trimmed.contains("%")) {
            try {
                return URLDecoder.decode(trimmed, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
        }
        return trimmed;
    }

    @PreDestroy
    public void close() {
        if (databaseReader != null) {
            try {
                databaseReader.close();
                log.info("[GeoIP] DatabaseReader fechado com sucesso.");
            } catch (IOException e) {
                log.warn("[GeoIP] Erro ao fechar DatabaseReader: {}", e.getMessage());
            }
        }
    }
}
