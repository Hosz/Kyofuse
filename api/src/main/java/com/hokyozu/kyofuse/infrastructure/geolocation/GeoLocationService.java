package com.hokyozu.kyofuse.infrastructure.geolocation;

import com.hokyozu.kyofuse.infrastructure.client.ClientIpResolver;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

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
            log.info("[GeoIP] Base de dados GeoLite2 não configurada (app.geoip.database-path vazia). Resolução funcionará em modo fallback.");
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

        String cleanedIp = clientIpResolver.cleanIp(rawIp);

        if (clientIpResolver.isLocalOrLoopback(cleanedIp)) {
            return LocationInfo.local(cleanedIp);
        }

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
