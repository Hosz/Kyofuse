export interface SavedAccount {
  userId: string;
  username: string;
  nickname: string;
  avatarUrl?: string;
  country?: string;
  showCountryFlag?: boolean;
  email?: string;
  role: string;
  switchToken?: string;
  lastActiveAt: string;
}

export interface SwitchAccountRequest {
  targetUserId: string;
  switchToken: string;
  deviceId: string;
}

export interface SwitchAccountResponse {
  userId: string;
  email: string;
  username: string;
  role: string;
  switchToken: string;
}

export interface DisconnectAccountRequest {
  targetUserId: string;
  deviceId: string;
}
