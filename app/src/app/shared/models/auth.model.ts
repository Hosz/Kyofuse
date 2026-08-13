export type AuthTabId = 'login' | 'register';

export interface AuthTab {
  id: AuthTabId;
  label: string;
}

export interface AuthFeature {
  icon: string;
  label: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
  birthDate: string;
}
