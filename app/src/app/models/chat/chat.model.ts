import { MessageMediaResponse } from '../media/message-media-response.model';
import { PostMediaItemRequest } from '../posts/post-request.model';

export type ConversationType = 'DIRECT' | 'GROUP' | 'COMMUNITY';

export type DirectConversationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED';

export interface ConversationResponse {
    id: string;
    type: ConversationType;
    name: string | null;
    avatarUrl: string | null;
    createdById: string;
    createdByUsername: string;
    communityId: string | null;
    communityName: string | null;
    communityAvatarUrl: string | null;
    directUserOneId: string | null;
    directUserOneUsername: string | null;
    directUserOneNickname: string | null;
    directUserOneAvatarUrl: string | null;
    directUserTwoId: string | null;
    directUserTwoUsername: string | null;
    directUserTwoNickname: string | null;
    directUserTwoAvatarUrl: string | null;
    directMessageStatus: DirectConversationStatus | null;
    revokedById: string | null;
    lastMessageContent?: string | null;
    lastMessageSenderUsername?: string | null;
    lastMessageSenderNickname?: string | null;
    lastMessageHasMedia?: boolean | null;
    lastMessageMediaType?: string | null;
    lastMessageCreatedAt?: string | null;
    unreadCount?: number | null;
    createdAt: string;
    updatedAt: string;
}

export interface ConversationRequest {
    name?: string;
    avatarUrl?: string;
    participantIds: string[];
}

export interface UpdateConversationRequest {
    name?: string;
    avatarUrl?: string;
}

export type MessageStatus = 'PENDING' | 'SENT' | 'DELIVERED' | 'READ';

export interface MessageResponse {
    id: string;
    conversationId: string;
    senderId: string;
    senderUsername: string;
    senderNickname: string | null;
    senderAvatarUrl: string | null;
    content: string;
    media?: MessageMediaResponse[];
    createdAt: string;
    status: MessageStatus;
}

export interface MessageReceiptItemResponse {
    userId: string;
    username: string;
    nickname: string;
    avatarUrl: string | null;
    deliveredAt: string | null;
    readAt: string | null;
}

export interface MessageInfoResponse {
    messageId: string;
    createdAt: string;
    receipts: MessageReceiptItemResponse[];
}

export interface MessageStatusEvent {
    messageId: string;
    conversationId: string;
    userId: string;
    status: MessageStatus;
    timestamp: string;
}

export interface MessageRequest {
    content?: string;
    media?: PostMediaItemRequest[];
}

export type ConversationMemberRole = 'ADMIN' | 'MEMBER';
export type ConversationMemberStatus = 'ACTIVE' | 'LEFT' | 'REMOVED' | 'KICKED';

export interface ConversationMemberResponse {
    id: string;
    conversationId: string;
    conversationName: string | null;
    userId: string;
    username: string;
    nickname: string | null;
    avatarUrl: string | null;
    role: ConversationMemberRole;
    status: ConversationMemberStatus;
    joinedAt: string;
    leftAt: string | null;
    lastReadAt: string | null;
    createdAt: string;
    updatedAt: string;
}
