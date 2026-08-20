import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { Observable } from 'rxjs';
import { postResponse } from '../../../models/posts/posts-response.model';
import { PageResponse } from '../../../models/page-response.model';
import { postRequest } from '../../../models/posts/post-request.model';

@Injectable({
  providedIn: 'root',
})
export class PostsService {

  api = API_URL;
  private url = `${this.api}/api/feed`;
  private http = inject(HttpClient);

  public getFeed(page: number = 0, size: number = 20): Observable<PageResponse<postResponse>> {
    const params = new HttpParams().set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<postResponse>>(this.url + '/posts', { params });
  }

  /** Feed de quem o usuário segue — só posts públicos, e nenhum post de comunidade. */
  public getFollowingPosts(page: number = 0, size: number = 20): Observable<PageResponse<postResponse>> {
    const params = new HttpParams().set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<postResponse>>(this.url + '/following/posts', { params });
  }

  public postContent(content: postRequest): Observable<postResponse> {
    return this.http.post<postResponse>(this.url + '/post', content);
  }

  /** Posts exclusivos de uma comunidade — não aparecem no feed geral nem no perfil. */
  public getCommunityPosts(communityId: string, page: number = 0, size: number = 20): Observable<PageResponse<postResponse>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    return this.http.get<PageResponse<postResponse>>(this.url + `/community/${communityId}/posts`, { params });
  }

  public postInCommunity(communityId: string, content: postRequest): Observable<postResponse> {
    return this.http.post<postResponse>(this.url + `/community/${communityId}/post`, content);
  }

  public getPost(postId: string): Observable<postResponse> {
    return this.http.get<postResponse>(this.url + `/post/${postId}`);
  }

  public getProfilePosts(profileId: string, page: number = 0, size: number = 20): Observable<PageResponse<postResponse>> {
    const params = new HttpParams().set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<postResponse>>(this.url + `/profile/${profileId}/posts`, { params });
  }

  public getMyPosts(page: number = 0, size: number = 20): Observable<PageResponse<postResponse>> {
    const params = new HttpParams().set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<postResponse>>(this.url + '/posts/me', { params });
  }

  /**
   * O backend expõe a exclusão como PATCH (é um soft delete — marca o post como
   * DELETED). Estava como DELETE aqui, o que devolveria 405.
   */
  public deletePost(postId: string): Observable<void> {
    return this.http.patch<void>(this.url + `/post/${postId}`, null);
  }

}
