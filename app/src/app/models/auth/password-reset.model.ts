export interface ForgotPasswordRequest {
  emailOrUsername: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}
