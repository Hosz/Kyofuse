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

export interface MessageResponse {
    id: string;
    conversationId: string;
    senderId: string;
    senderUsername: string;
    senderNickname: string | null;
    senderAvatarUrl: string | null;
    content: string;
    createdAt: string;
}

export interface MessageRequest {
    content: string;
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
