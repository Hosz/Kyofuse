export type ProfileVisibility = 'PUBLIC' | 'FOLLOWERS' | 'FRIENDS' | 'PRIVATE';
export type MessagePermission = 'EVERYONE' | 'FOLLOWERS' | 'FRIENDS' | 'NOBODY';
export type FriendRequestPermission = 'EVERYONE' | 'NOBODY';
export type TeamInvitePermission = 'EVERYONE' | 'FRIENDS' | 'NOBODY';

export const PROFILE_VISIBILITY_OPTIONS: { value: ProfileVisibility; label: string }[] = [
  { value: 'PUBLIC', label: 'Público' },
  { value: 'FOLLOWERS', label: 'Seguidores' },
  { value: 'FRIENDS', label: 'Amigos' },
  { value: 'PRIVATE', label: 'Privado' },
];

export const MESSAGE_PERMISSION_OPTIONS: { value: MessagePermission; label: string }[] = [
  { value: 'EVERYONE', label: 'Todos' },
  { value: 'FOLLOWERS', label: 'Seguidores' },
  { value: 'FRIENDS', label: 'Amigos' },
  { value: 'NOBODY', label: 'Ninguém' },
];

export const FRIEND_REQUEST_PERMISSION_OPTIONS: { value: FriendRequestPermission; label: string }[] = [
  { value: 'EVERYONE', label: 'Todos' },
  { value: 'NOBODY', label: 'Ninguém' },
];

export const TEAM_INVITE_PERMISSION_OPTIONS: { value: TeamInvitePermission; label: string }[] = [
  { value: 'EVERYONE', label: 'Todos' },
  { value: 'FRIENDS', label: 'Amigos' },
  { value: 'NOBODY', label: 'Ninguém' },
];
