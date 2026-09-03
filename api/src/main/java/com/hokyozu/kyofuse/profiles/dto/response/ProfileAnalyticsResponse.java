package com.hokyozu.kyofuse.profiles.dto.response;

import java.util.UUID;

public record ProfileAnalyticsResponse(
        UUID profileId,
        long dailyUniqueVisitors,
        long monthlyUniqueVisitors
) {}
