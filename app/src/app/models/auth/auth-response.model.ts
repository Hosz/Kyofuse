export interface RequestFail {
    error: {
        code: number,
        message: string,
        extra?: {
            error: string
        }
    }
}

export interface authResponse {
  userId: string;
  email: string;
  username: string;
  role: string;
}

export interface authMeResponse {
  userId: string;
  email: string;
  username: string;
  role: string;
  totpEnabled: boolean;
  profileSetupStatus: string;
}

export interface mfaRequiredResponse {
  mfaRequired: true;
  mfaToken: string;
}

/** Resultado de POST /api/auth/login: ou autentica de vez, ou pede o código de 2FA. */
export type loginResult = authResponse | mfaRequiredResponse;

export function isMfaRequired(result: loginResult): result is mfaRequiredResponse {
  return (result as mfaRequiredResponse).mfaRequired === true;
}

export interface RegisterResponse {
  userId: string;
  email: string;
  username: string;
  emailVerified: boolean;
  message: string;
}

export interface VerifyEmailRequest {
  token: string;
}

export interface ResendVerificationRequest {
  emailOrUsername: string;
}

