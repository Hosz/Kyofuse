export interface UserBlockResponse {
    status: string;
    blockerId: string;
    blockerUsername: string;
    blockedId: string;
    blockedUsername: string;
    blockedNickname: string | null;
    blockedAvatarUrl: string | null;
    createdAt: string;
}