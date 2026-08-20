export type CommunityVisibility = 'PUBLIC' | 'PRIVATE';
export type CommunityStatus = 'ACTIVE' | 'ARCHIVED';
export type CommunityMemberRole = 'ADMIN' | 'MODERATOR' | 'MEMBER';
export type CommunityMemberStatus = 'ACTIVE' | 'LEFT' | 'REMOVED' | 'KICKED' | 'BANNED';

export interface CommunityResponse {
    id: string;
    communityName: string;
    communitySlug: string;
    communityDescription: string | null;
    communityAvatarUrl: string | null;
    communityBannerUrl: string | null;
    ownerId: string;
    ownerUsername: string;
    teamId: string | null;
    teamName: string | null;
    teamAvatarUrl: string | null;
    visibility: CommunityVisibility;
    status: CommunityStatus;
    createdAt: string;
    updatedAt: string;
}

export interface CommunityRequest {
    communityName: string;
    communitySlug: string;
    communityDescription?: string;
    communityAvatarUrl?: string;
    communityBannerUrl?: string;
    visibility: CommunityVisibility;
}

export interface UpdateCommunityRequest {
    communityName?: string;
    communitySlug?: string;
    communityDescription?: string;
    communityAvatarUrl?: string;
    communityBannerUrl?: string;
    visibility?: CommunityVisibility;
}

export interface CommunityMemberResponse {
    id: string;
    communityId: string;
    communityName: string;
    communitySlug: string;
    memberId: string;
    memberUsername: string;
    memberNickname: string | null;
    memberAvatarUrl: string | null;
    role: CommunityMemberRole;
    status: CommunityMemberStatus;
    joinedAt: string;
    leftAt: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface CommunityJoinRequestResponse {
    id: string;
    communityId: string;
    communityName: string;
    communitySlug: string;
    userId: string;
    username: string;
    createdAt: string;
}
