package com.hokyozu.kyofuse.relationships.privacy.dto.response;

import com.hokyozu.kyofuse.relationships.privacy.enums.*;

public record UserPrivacySettingsResponse(
        ProfileVisibility profileVisibility,
        MessagePermission messagePermission,
        FriendRequestPermission friendRequestPermission,
        TeamInvitePermission teamInvitePermission
) {
}
