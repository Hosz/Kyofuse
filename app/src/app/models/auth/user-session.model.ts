export type DeviceType = 'WINDOWS' | 'MACOS' | 'LINUX' | 'ANDROID' | 'IOS' | 'UNKNOWN';

export interface UserSession {
  id: string;
  deviceId: string;
  deviceType: DeviceType;
  deviceName: string;
  browser: string;
  os: string;
  ipAddress: string;
  location: string;
  trusted: boolean;
  trustedAt?: string;
  lastActiveAt: string;
  createdAt: string;
  current: boolean;
}
