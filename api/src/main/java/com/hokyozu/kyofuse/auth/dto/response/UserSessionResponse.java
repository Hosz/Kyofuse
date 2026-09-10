package com.hokyozu.kyofuse.auth.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserSessionResponse(
        UUID id,
        String deviceId,
        String deviceType,
        String deviceName,
        String browser,
        String os,
        String ipAddress,
        String location,
        boolean trusted,
        Instant trustedAt,
        Instant lastActiveAt,
        Instant createdAt,
        boolean current
) {}
