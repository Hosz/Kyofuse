import { ReactionType } from '../../shared/models/reaction.model';

export interface PostReactionResponse {
    postId: string;
    username: string;
    nickname: string;
    profileImage: string;
    reactionType: ReactionType;
}

export interface PostReactionRequest {
    reactionType: ReactionType;
}