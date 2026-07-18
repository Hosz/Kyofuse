package com.hokyozu.kyofuse.relationships.privacy.dto.request;

import com.hokyozu.kyofuse.relationships.privacy.enums.*;

public record UserPrivacySettingsRequest(
        ProfileVisibility profileVisibility,
        ProfileVisibility postVisibility,
        ProfileVisibility followersVisibility,
        ProfileVisibility followingVisibility,
        MessagePermission messagePermission,
        FriendRequestPermission friendRequestPermission,
        FollowPermission followPermission,
        TeamInvitePermission teamInvitePermission,
        DuoInvitePermission duoInvitePermission
) {
}
