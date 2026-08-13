import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { PageResponse } from '../../../models/page-response.model';
import { UserFriendRequestResponse } from '../../../models/friendship/friendship-request-response.model';

@Injectable({
  providedIn: 'root',
})
export class FriendshipRequestService {

  api = API_URL;
  private url = `${this.api}/api/user-friend-request`;
  private http = inject(HttpClient);

  public sendRequest(userId: string) {
    return this.http.post<UserFriendRequestResponse>(`${this.url}/${userId}/send`, null);
  }

  public showRequests() {
    return this.http.get<PageResponse<UserFriendRequestResponse>>(`${this.url}/requests-recieved`);
  }

  public showSentRequests() {
    return this.http.get<PageResponse<UserFriendRequestResponse>>(`${this.url}/requests-sent`);
  }

  public removeRequests(userId: string) {
    return this.http.delete<void>(`${this.url}/${userId}/remove-request`);
  }
}
