export interface CommentRequest {
  content: string;
}

export interface CommentResponse {
    id: string;
    postId: string;
    profileImage: string;
    authorId: string;
    authorNickname: string;
    authorUsername: string;
    content: string;
    commentStatus: string;
    reactionCount: number;
    likeCount: number;
    currentUserReaction?: string | null;
    createdAt: string;
    updatedAt: string;
}