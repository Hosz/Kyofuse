import {
    DuoInvitePermission,
    FollowPermission,
    FriendRequestPermission,
    MessagePermission,
    ProfileVisibility,
    TeamInvitePermission,
} from '../../shared/models/privacy-options.model';

export interface UserPrivacySettingsResponse {
    profileVisibility: ProfileVisibility;
    postVisibility: ProfileVisibility;
    likesVisibility: ProfileVisibility;
    repostsVisibility: ProfileVisibility;
    friendsVisibility: ProfileVisibility;
    followersVisibility: ProfileVisibility;
    followingVisibility: ProfileVisibility;
    messagePermission: MessagePermission;
    friendRequestPermission: FriendRequestPermission;
    followPermission: FollowPermission;
    teamInvitePermission: TeamInvitePermission;
    duoInvitePermission: DuoInvitePermission;
}

export interface UserPrivacySettingsRequest {
    profileVisibility: ProfileVisibility;
    postVisibility: ProfileVisibility;
    likesVisibility: ProfileVisibility;
    repostsVisibility: ProfileVisibility;
    friendsVisibility: ProfileVisibility;
    followersVisibility: ProfileVisibility;
    followingVisibility: ProfileVisibility;
    messagePermission: MessagePermission;
    friendRequestPermission: FriendRequestPermission;
    followPermission: FollowPermission;
    teamInvitePermission: TeamInvitePermission;
    duoInvitePermission: DuoInvitePermission;
}
