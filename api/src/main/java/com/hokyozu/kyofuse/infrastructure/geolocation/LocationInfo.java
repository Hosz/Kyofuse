package com.hokyozu.kyofuse.infrastructure.geolocation;

import java.util.ArrayList;
import java.util.List;

public record LocationInfo(
        String ip,
        String city,
        String state,
        String country,
        String countryCode,
        boolean isLocal,
        String formattedLocation
) {
    public static LocationInfo local(String ip) {
        return new LocationInfo(ip, "Ambiente Local", "Local", "Rede Local", "LOC", true, "Rede Local (Desenvolvimento)");
    }

    public static LocationInfo unknown(String ip) {
        return new LocationInfo(ip, null, null, null, null, false, "Localização não identificada");
    }

    public static LocationInfo of(String ip, String city, String state, String country, String countryCode) {
        List<String> parts = new ArrayList<>();
        if (city != null && !city.isBlank()) {
            parts.add(city.trim());
        }
        if (state != null && !state.isBlank() && (city == null || !state.trim().equalsIgnoreCase(city.trim()))) {
            parts.add(state.trim());
        }
        if (country != null && !country.isBlank()) {
            parts.add(country.trim());
        }

        String formatted = parts.isEmpty() ? "Localização não identificada" : String.join(", ", parts);
        return new LocationInfo(ip, city, state, country, countryCode, false, formatted);
    }
}
