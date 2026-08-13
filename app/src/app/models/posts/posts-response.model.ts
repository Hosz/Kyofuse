export interface postResponse  {
    id: string;
    authorId: string;
    authorNickname: string;
    authorUsername: string;
    authorAvatarUrl: string;
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

