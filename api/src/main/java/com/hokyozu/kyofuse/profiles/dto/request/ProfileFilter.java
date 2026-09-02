package com.hokyozu.kyofuse.profiles.dto.request;

import com.hokyozu.kyofuse.users.enums.UserStatus;

public record ProfileFilter(
        String username,
        UserStatus status
) {
}
