import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import {
  TeamInviteCancelRequest,
  TeamInviteRequest,
  TeamInviteResponse,
  TeamInviteStatus,
} from '../../../models/teams/team-invite.model';

@Injectable({
  providedIn: 'root',
})
export class TeamInviteService {

  api = API_URL;
  private url = `${this.api}/api/team-invite`;
  private http = inject(HttpClient);

  public inviteUser(teamId: string, receiverUsername: string, request: TeamInviteRequest) {
    return this.http.post<TeamInviteResponse>(`${this.url}/${teamId}/invite/${receiverUsername}`, request);
  }

  public listInvites(teamId: string, status?: TeamInviteStatus) {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<PageResponse<TeamInviteResponse>>(`${this.url}/${teamId}/invites`, { params });
  }

  public acceptInvite(inviteId: string) {
    return this.http.patch<void>(`${this.url}/${inviteId}/accept`, null);
  }

  public declineInvite(inviteId: string) {
    return this.http.patch<void>(`${this.url}/${inviteId}/decline`, null);
  }

  public cancelInvite(inviteId: string, request: TeamInviteCancelRequest) {
    return this.http.patch<void>(`${this.url}/${inviteId}/cancel`, request);
  }
}
