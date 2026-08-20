import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { followResponse } from '../../../models/follow/follow-response.model';
import { PageResponse } from '../../../models/page-response.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class FollowService {

  api = API_URL;
  private url = `${this.api}/api/user-follow`;
  private http = inject(HttpClient);

  public followUser(userId: string) {
    return this.http.post<followResponse>(`${this.url}/${userId}/follow`, {});
  }

  public showFollowers(userId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<followResponse>>(`${this.url}/${userId}/followers`, { params });
  }

  /** O backend expõe isso como Long (aceita nulo), por isso o tipo aqui inclui null. */
  public showFollowersQuantity(userId: string) {
    return this.http.get<number | null>(`${this.url}/${userId}/followers/quantity`);
  }

  public showMyFollowers(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<followResponse>>(`${this.url}/me/followers`, { params });
  }

  public showMyFollowersQuantity() {
    return this.http.get<number | null>(`${this.url}/me/followers/quantity`);
  }

  /** Se eu já sigo esse usuário — o botão precisa disso pra nascer como "Seguindo". */
  public isFollowing(userId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.url}/${userId}/is-following`);
  }

  public unfollowUser(userId: string) {
    return this.http.delete<void>(`${this.url}/${userId}/unfollow`);
  }

  public showMyFollowing(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<followResponse>>(`${this.url}/me/following`, { params });
  }

  public showMyFollowingQuantity() {
    return this.http.get<number | null>(`${this.url}/me/following/quantity`);
  }

  public showFollowing(userId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<followResponse>>(`${this.url}/${userId}/following`, { params });
  }

  public showFollowingQuantity(userId: string) {
    return this.http.get<number | null>(`${this.url}/${userId}/following/quantity`);
  }

  public removeFollower(userId: string) {
    return this.http.delete<void>(`${this.url}/${userId}/delete`);
  }

  public rejectFollowRequest(requestId: string) {
    return this.http.delete<void>(`${this.url}/${requestId}/reject`);
  }

  public acceptFollowRequest(requestId: string) {
    return this.http.patch<followResponse>(`${this.url}/${requestId}/accept`, {});
  }
}
