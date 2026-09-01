package com.hokyozu.kyofuse.relationships.privacy.mapper;

import com.hokyozu.kyofuse.relationships.privacy.dto.request.UserPrivacySettingsRequest;
import com.hokyozu.kyofuse.relationships.privacy.dto.response.UserPrivacySettingsResponse;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.*;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;

import java.time.Instant;

public class UserPrivacySettingsMapper {
    public static void toUpdate(UserPrivacySettings settings, @Valid UserPrivacySettingsRequest request) {
        if (request.profileVisibility() != null) {
            settings.setProfileVisibility(request.profileVisibility());
        }
        if (request.messagePermission() != null) {
            settings.setMessagePermission(request.messagePermission());
        }
        if (request.friendRequestPermission() != null) {
            settings.setFriendRequestPermission(request.friendRequestPermission());
        }
        if (request.teamInvitePermission() != null) {
            settings.setTeamInvitePermission(request.teamInvitePermission());
        }
        settings.setUpdatedAt(Instant.now());
    }

    public static UserPrivacySettingsResponse toResponse(UserPrivacySettings settings) {
        return new UserPrivacySettingsResponse(
                settings.getProfileVisibility(),
                settings.getMessagePermission(),
                settings.getFriendRequestPermission(),
                settings.getTeamInvitePermission()
        );
    }

    public static UserPrivacySettings createDefault(User user) {
        return UserPrivacySettings.builder()
                .user(user)
                .profileVisibility(ProfileVisibility.PUBLIC)
                .messagePermission(MessagePermission.EVERYONE)
                .friendRequestPermission(FriendRequestPermission.EVERYONE)
                .teamInvitePermission(TeamInvitePermission.EVERYONE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
