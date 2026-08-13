import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { friendshipResponse } from '../../../models/friendship/friendship-response.model';

@Injectable({
  providedIn: 'root',
})
export class FriendshipService {

  api = API_URL;
  private url = `${this.api}/api/user-friendship`;
  private http = inject(HttpClient);

  public acceptRequest(requestId: string) {
    return this.http.post<friendshipResponse>(`${this.url}/${requestId}/accept-request`, {});
  }

  public declineRequest(requestId: string) {
    return this.http.delete<void>(`${this.url}/${requestId}/decline-request`, {});
  }

  public showMyFriends(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<friendshipResponse>>(`${this.url}/me/friends`, { params });
  }

  public showUserFriends(userId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<friendshipResponse>>(`${this.url}/${userId}/friends`, { params });
  }

  public removeFriendship(userId: string) {
    return this.http.delete<void>(`${this.url}/${userId}/remove`);
  }

  public showMyFriendsQuantity() {
    return this.http.get<number>(`${this.url}/me/friends/quantity`);
  }

  public showUserFriendsQuantity(userId: string) {
    return this.http.get<number>(`${this.url}/${userId}/friends/quantity`);
  }
}
