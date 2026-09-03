export type PresenceStatus = 'ONLINE' | 'OFFLINE';

export interface PresenceResponse {
  userId: string;
  status: PresenceStatus;
  lastSeen: string | null;
}

export interface BatchPresenceRequest {
  userIds: string[];
}
