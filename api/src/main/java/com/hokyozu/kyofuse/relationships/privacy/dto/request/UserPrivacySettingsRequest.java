package com.hokyozu.kyofuse.relationships.privacy.dto.request;

import com.hokyozu.kyofuse.relationships.privacy.enums.*;

public record UserPrivacySettingsRequest(
        ProfileVisibility profileVisibility,
        MessagePermission messagePermission,
        FriendRequestPermission friendRequestPermission,
        TeamInvitePermission teamInvitePermission
) {
}
