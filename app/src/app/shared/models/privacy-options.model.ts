export type ProfileVisibility = 'PUBLIC' | 'FOLLOWERS' | 'FRIENDS' | 'PRIVATE';
export type MessagePermission = 'EVERYONE' | 'NOBODY';
export type FriendRequestPermission = 'EVERYONE' | 'NOBODY';
export type TeamInvitePermission = 'EVERYONE' | 'FRIENDS' | 'NOBODY';

export const PROFILE_VISIBILITY_OPTIONS: { value: ProfileVisibility; label: string; key: string }[] = [
  { value: 'PUBLIC', label: 'Público', key: 'settings.visibilityPublic' },
  { value: 'FOLLOWERS', label: 'Seguidores', key: 'settings.visibilityFollowers' },
  { value: 'FRIENDS', label: 'Amigos', key: 'settings.visibilityFriends' },
  { value: 'PRIVATE', label: 'Privado', key: 'settings.visibilityPrivate' },
];

export const MESSAGE_PERMISSION_OPTIONS: { value: MessagePermission; label: string; key: string }[] = [
  { value: 'EVERYONE', label: 'Todos', key: 'settings.permissionEveryone' },
  { value: 'NOBODY', label: 'Ninguém', key: 'settings.permissionNobody' },
];

export const FRIEND_REQUEST_PERMISSION_OPTIONS: { value: FriendRequestPermission; label: string; key: string }[] = [
  { value: 'EVERYONE', label: 'Todos', key: 'settings.permissionEveryone' },
  { value: 'NOBODY', label: 'Ninguém', key: 'settings.permissionNobody' },
];

export const TEAM_INVITE_PERMISSION_OPTIONS: { value: TeamInvitePermission; label: string; key: string }[] = [
  { value: 'EVERYONE', label: 'Todos', key: 'settings.permissionEveryone' },
  { value: 'FRIENDS', label: 'Amigos', key: 'settings.permissionFriends' },
  { value: 'NOBODY', label: 'Ninguém', key: 'settings.permissionNobody' },
];
