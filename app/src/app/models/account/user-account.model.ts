export interface UserAccountResponse {
  id: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  emailVerified: boolean;
  isSyntheticEmail: boolean;
  hasPassword: boolean;
  hasSteam: boolean;
  hasGoogle: boolean;
  totpEnabled: boolean;
  createdAt: string;
}

export interface UpdateUsernameRequest {
  username: string;
}

export interface UpdateEmailRequest {
  email: string;
  currentPassword?: string;
}

export interface ChangePasswordRequest {
  currentPassword?: string;
  newPassword: string;
}
