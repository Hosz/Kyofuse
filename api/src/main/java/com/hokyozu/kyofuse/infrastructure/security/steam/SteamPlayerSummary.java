package com.hokyozu.kyofuse.infrastructure.security.steam;

public record SteamPlayerSummary(
        String steamId,
        String personaName,
        String profileUrl,
        String avatarFull,
        String locCountryCode
) {
}
