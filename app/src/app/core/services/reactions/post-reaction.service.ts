import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PostReactionRequest, PostReactionResponse } from '../../../models/reactions/post-reaction.model';
import { PageResponse } from '../../../models/page-response.model';

@Injectable({
  providedIn: 'root',
})
export class PostReactionService {
  api = API_URL;
  private url = `${this.api}/api/posts/reactions`;
  private http = inject(HttpClient);

  public upsertPostReaction(postId: string, reactionType: PostReactionRequest) {
    return this.http.post<PostReactionResponse>(`${this.url}/${postId}`, reactionType);
  }

  public removeReaction(postId: string) {
    return this.http.delete<void>(`${this.url}/${postId}/remove`);
  }

  public getLikes(postId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<PostReactionResponse>>(`${this.url}/${postId}/likes`, { params });
  }

  public getReactions(postId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<PostReactionResponse>>(`${this.url}/${postId}/reactions`, { params });
  }
}
