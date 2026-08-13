import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { PageResponse } from '../../../models/page-response.model';
import { TeamMemberEditRequest, TeamMemberResponse } from '../../../models/teams/team-member.model';

@Injectable({
  providedIn: 'root',
})
export class TeamMemberService {

  api = API_URL;
  private url = `${this.api}/api/team-member/`
  private http = inject(HttpClient);

  public addMember(teamId: string, userId: string) {
    return this.http.post<TeamMemberResponse>(`${this.url}${teamId}/add/${userId}`, null);
  }

  public editMember(teamId: string, userId: string, request: TeamMemberEditRequest) {
    return this.http.patch<TeamMemberResponse>(`${this.url}${teamId}/edit/${userId}`, request);
  }

  public listMembers(teamId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<TeamMemberResponse>>(`${this.url}${teamId}/members`, { params });
  }

  public detailMember(teamId: string, teamMemberId: string) {
    return this.http.get<TeamMemberResponse>(`${this.url}${teamId}/${teamMemberId}`);
  }

  public removeMember(teamId: string, userId: string) {
    return this.http.delete<void>(`${this.url}${teamId}/${userId}/remove`);
  }

  public leaveTeam(teamId: string) {
    return this.http.delete<void>(`${this.url}${teamId}/leave`);
  }
}
