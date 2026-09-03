package com.hokyozu.kyofuse.presence.dto.request;

import java.util.List;
import java.util.UUID;

public record BatchPresenceRequest(
        List<UUID> userIds
) {}
