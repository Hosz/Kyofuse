export interface FriendshipStatusResponse {
  isFriend: boolean;
  requestSent: boolean;
  requestReceived: boolean;
  requestId?: string | null;
}
