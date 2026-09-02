import {
  FriendRequestPermission,
  MessagePermission,
  ProfileVisibility,
  TeamInvitePermission,
} from '../../shared/models/privacy-options.model';

export interface UserPrivacySettingsResponse {
  profileVisibility: ProfileVisibility;
  messagePermission: MessagePermission;
  friendRequestPermission: FriendRequestPermission;
  teamInvitePermission: TeamInvitePermission;
}

export interface UserPrivacySettingsRequest {
  profileVisibility: ProfileVisibility;
  messagePermission: MessagePermission;
  friendRequestPermission: FriendRequestPermission;
  teamInvitePermission: TeamInvitePermission;
}
