import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { API_URL } from '../../../models/api-url.model';
import { PageResponse } from '../../../models/page-response.model';
import {
  CommunityInviteCancelRequest,
  CommunityInviteRequest,
  CommunityInviteResponse,
  CommunityInviteStatus,
} from '../../../models/communities/community-invite.model';

@Injectable({
  providedIn: 'root',
})
export class CommunityInviteService {
  private api = API_URL;
  private url = `${this.api}/api/community-invite`;
  private http = inject(HttpClient);

  public inviteUser(communityIdentifier: string, receiverUsername: string, request?: CommunityInviteRequest) {
    return this.http.post<CommunityInviteResponse>(
      `${this.url}/${communityIdentifier}/invite/${receiverUsername}`,
      request ?? {}
    );
  }

  public listInvites(communityIdentifier: string, status?: CommunityInviteStatus) {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<PageResponse<CommunityInviteResponse>>(
      `${this.url}/${communityIdentifier}/invites`,
      { params }
    );
  }

  public acceptInvite(inviteId: string) {
    return this.http.patch<void>(`${this.url}/${inviteId}/accept`, null);
  }

  public declineInvite(inviteId: string) {
    return this.http.patch<void>(`${this.url}/${inviteId}/decline`, null);
  }

  public cancelInvite(inviteId: string, request?: CommunityInviteCancelRequest) {
    return this.http.patch<void>(`${this.url}/${inviteId}/cancel`, request ?? {});
  }
}
