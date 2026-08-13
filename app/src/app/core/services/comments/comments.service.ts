import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CommentRequest, CommentResponse } from '../../../models/comments/comments.model';
import { PageResponse } from '../../../models/page-response.model';

@Injectable({
  providedIn: 'root',
})
export class CommentsService {

  api = API_URL;
  private url = `${this.api}/api/comments`;
  private http = inject(HttpClient);

  public postComment(postId: string, commentRequest: CommentRequest) {
    return this.http.post<CommentResponse>(`${this.url}/post/${postId}`, commentRequest);
  }

  public getComment(commentId: string) {
    return this.http.get<CommentResponse>(`${this.url}/${commentId}`);
  }

  public listComments(postId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<CommentResponse>>(`${this.url}/post/${postId}/comments`, { params });
  }

  public deleteComment(postId: string, commentId: string) {
    return this.http.delete<void>(`${this.url}/${postId}/${commentId}/delete`);
  }

  public listUserComments(userId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<CommentResponse>>(`${this.url}/user/${userId}/comments`, { params });
  }
}
