export interface postResponse  {
    id: string;
    authorId: string;
    authorNickname: string;
    authorUsername: string;
    authorAvatarUrl: string;
    communityId: string | null;
    communityName: string | null;
    content: string;
    postType: string;
    postVisibility: string;
    postStatus: string;
    reactionCount: number;
    likeCount: number;
    commentCount: number;
    maps: string[];
    createdAt: string;
    updatedAt: string;
}

