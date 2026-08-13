import { ReactionType } from '../../shared/models/reaction.model';

export interface CommentReactionRequest {
    reactionType: ReactionType;
}

export interface CommentReactionResponse {
    postId: string;
    commentId: string;
    username: string;
    nickname: string;
    profileImage: string;
    reactionType: ReactionType;
}