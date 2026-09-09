package com.hokyozu.kyofuse.auth.event;

import com.hokyozu.kyofuse.infrastructure.geolocation.LocationInfo;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public record UserLoginSuccessEvent(
        User user,
        String clientIp,
        String userAgent,
        Instant loggedAt,
        LocationInfo location
) {
    public UserLoginSuccessEvent(User user, String clientIp, String userAgent, Instant loggedAt) {
        this(user, clientIp, userAgent, loggedAt, null);
    }
}
