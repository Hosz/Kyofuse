export type CommunityInviteStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CANCELED' | 'EXPIRED';

export interface CommunityInviteResponse {
  id: string;
  communityId: string;
  communityName: string;
  communitySlug: string;
  communityAvatarUrl: string | null;
  senderId: string;
  senderName: string;
  receiverId: string;
  receiverName: string;
  status: CommunityInviteStatus;
  message: string | null;
  createdAt: string;
}

export interface CommunityInviteRequest {
  message?: string;
}

export interface CommunityInviteCancelRequest {
  cancellationReason?: string;
}
