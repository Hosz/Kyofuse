import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CommentReactionRequest, CommentReactionResponse } from '../../../models/reactions/comment-reaction.model';
import { PageResponse } from '../../../models/page-response.model';


@Injectable({
  providedIn: 'root',
})
export class CommentReactionService {
    api = API_URL;
    private url = `${this.api}/api/comments/reactions`;
    private http = inject(HttpClient);

    public upsertCommentReaction(postId: string, commentId: string, reactionType: CommentReactionRequest) {
        return this.http.post<CommentReactionResponse>(`${this.url}/${postId}/${commentId}`, reactionType);
    }

    public removeCommentReaction(postId: string, commentId: string) {
        return this.http.delete<void>(`${this.url}/${postId}/${commentId}`);
    }

    public getLikes(postId: string, commentId: string, page: number = 0, size: number = 20) {
        const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
        return this.http.get<PageResponse<CommentReactionResponse>>(`${this.url}/${postId}/${commentId}/likes`, { params });
    }

    public getReactions(postId: string, commentId: string, page: number = 0, size: number = 20) {
        const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
        return this.http.get<PageResponse<CommentReactionResponse>>(`${this.url}/${postId}/${commentId}/reactions`, { params });
    }
}
