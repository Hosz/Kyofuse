import { PostMediaResponse } from '../media/post-media-response.model';

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
    viewCount?: number;
    maps: string[];
    media: PostMediaResponse[];
    currentUserReaction?: string | null;
    createdAt: string;
    updatedAt: string;
}
