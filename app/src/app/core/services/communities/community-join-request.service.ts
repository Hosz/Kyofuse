import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { CommunityJoinRequestResponse } from '../../../models/communities/community.model';

@Injectable({
  providedIn: 'root',
})
export class CommunityJoinRequestService {

  api = API_URL;
  private url = `${this.api}/api/communities/join-requests`;
  private http = inject(HttpClient);

  public listJoinRequests(communityId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<CommunityJoinRequestResponse>>(`${this.url}/${communityId}`, { params });
  }

  public requestToJoinCommunity(communityId: string) {
    return this.http.post<CommunityJoinRequestResponse>(`${this.url}/${communityId}/request`, {});
  }

  public approveJoinRequest(requestId: string) {
    return this.http.post<void>(`${this.url}/${requestId}/approve`, {});
  }

  public rejectJoinRequest(requestId: string) {
    return this.http.delete<void>(`${this.url}/${requestId}/reject`);
  }
}
