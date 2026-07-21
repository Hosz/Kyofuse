package com.hokyozu.kyofuse.relationships.privacy.dto.response;

import com.hokyozu.kyofuse.relationships.privacy.enums.*;

public record UserPrivacySettingsResponse(
        ProfileVisibility profileVisibility,
        ProfileVisibility postVisibility,
        ProfileVisibility likesVisibility,
        ProfileVisibility repostsVisibility,
        ProfileVisibility friendsVisibility,
        ProfileVisibility followersVisibility,
        ProfileVisibility followingVisibility,
        MessagePermission messagePermission,
        FriendRequestPermission friendRequestPermission,
        FollowPermission followPermission,
        TeamInvitePermission teamInvitePermission,
        DuoInvitePermission duoInvitePermission
) {
}
