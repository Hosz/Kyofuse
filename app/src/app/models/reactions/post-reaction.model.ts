import { ReactionType } from '../../shared/models/reaction.model';

export interface PostReactionResponse {
    postId: string;
    userId: string;
    username: string;
    nickname: string;
    profileImage: string;
    reactionType: ReactionType;
}

export interface PostReactionRequest {
    reactionType: ReactionType;
}