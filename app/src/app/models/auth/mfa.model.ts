export interface mfaVerifyRequest {
  mfaToken: string;
  code: string;
}

export interface totpSetupResponse {
  secret: string;
  otpauthUri: string;
}

export interface totpConfirmResponse {
  recoveryCodes: string[];
}

export interface disable2faRequest {
  password: string;
}
