package com.hokyozu.kyofuse.relationships.privacy.mapper;

import com.hokyozu.kyofuse.relationships.privacy.dto.request.UserPrivacySettingsRequest;
import com.hokyozu.kyofuse.relationships.privacy.dto.response.UserPrivacySettingsResponse;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import jakarta.validation.Valid;

public class UserPrivacySettingsMapper {
    public static void toUpdate(UserPrivacySettings settings, @Valid UserPrivacySettingsRequest request) {
        if (request.profileVisibility() != null) {
            settings.setProfileVisibility(request.profileVisibility());
        }
        if (request.postVisibility() != null) {
            settings.setPostsVisibility(request.postVisibility());
        }
        if (request.followersVisibility() != null) {
            settings.setFollowersVisibility(request.followersVisibility());
        }
        if (request.followingVisibility() != null) {
            settings.setFollowingVisibility(request.followingVisibility());
        }
        if (request.messagePermission() != null) {
            settings.setMessagePermission(request.messagePermission());
        }
        if (request.friendRequestPermission() != null) {
            settings.setFriendRequestPermission(request.friendRequestPermission());
        }
        if (request.followPermission() != null) {
            settings.setFollowPermission(request.followPermission());
        }
        if (request.teamInvitePermission() != null) {
            settings.setTeamInvitePermission(request.teamInvitePermission());
        }
        if (request.duoInvitePermission() != null) {
            settings.setDuoInvitePermission(request.duoInvitePermission());
        }
    }

    public static UserPrivacySettingsResponse toResponse(UserPrivacySettings settings) {
        return new UserPrivacySettingsResponse(
                settings.getProfileVisibility(),
                settings.getPostsVisibility(),
                settings.getFollowersVisibility(),
                settings.getFollowingVisibility(),
                settings.getMessagePermission(),
                settings.getFriendRequestPermission(),
                settings.getFollowPermission(),
                settings.getTeamInvitePermission(),
                settings.getDuoInvitePermission()
        );
    }
}
