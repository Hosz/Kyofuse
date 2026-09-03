package com.hokyozu.kyofuse.presence.dto.response;

import com.hokyozu.kyofuse.presence.enums.PresenceStatus;

import java.time.Instant;
import java.util.UUID;

public record PresenceResponse(
        UUID userId,
        PresenceStatus status,
        Instant lastSeen
) {}
