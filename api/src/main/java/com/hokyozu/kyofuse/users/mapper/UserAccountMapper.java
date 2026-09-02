package com.hokyozu.kyofuse.users.mapper;

import com.hokyozu.kyofuse.users.dto.response.UserAccountResponse;
import com.hokyozu.kyofuse.users.entity.User;

public class UserAccountMapper {

    private UserAccountMapper() {}

    public static UserAccountResponse toResponse(User user) {
        boolean hasPassword = user.isHasCustomPassword();
        boolean hasSteam = user.getSteamId() != null && !user.getSteamId().isBlank();
        boolean hasGoogle = user.getGoogleId() != null && !user.getGoogleId().isBlank();
        boolean isSyntheticEmail = user.getEmail() != null && user.getEmail().toLowerCase().endsWith("@steam.kyofuse.local");

        return new UserAccountResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.isEmailVerified(),
                isSyntheticEmail,
                hasPassword,
                hasSteam,
                hasGoogle,
                user.isTotpEnabled(),
                user.getCreatedAt()
        );
    }
}
