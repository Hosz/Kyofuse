import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { FriendRequestResponse } from '../../../models/friendship/friend-request.model';

@Injectable({
  providedIn: 'root',
})
export class FriendRequestService {

  api = API_URL;
  private url = `${this.api}/api/user-friend-request`;
  private http = inject(HttpClient);

  public sendRequest(userId: string) {
    return this.http.post<FriendRequestResponse>(`${this.url}/${userId}/send`, {});
  }

  public showReceivedRequests(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<FriendRequestResponse>>(`${this.url}/requests-recieved`, { params });
  }

  public showSentRequests(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<FriendRequestResponse>>(`${this.url}/requests-sent`, { params });
  }

  public removeRequest(requestId: string) {
    return this.http.delete<void>(`${this.url}/${requestId}/remove-request`);
  }
}
