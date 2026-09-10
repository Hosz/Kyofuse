import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { CommunityMemberResponse } from '../../../models/communities/community.model';

@Injectable({
  providedIn: 'root',
})
export class CommunityMemberService {

  api = API_URL;
  private url = `${this.api}/api/communities/members`;
  private http = inject(HttpClient);

  public joinCommunity(communityId: string) {
    return this.http.post<CommunityMemberResponse>(`${this.url}/${communityId}/join`, {});
  }

  public listCommunityMembers(communityId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<CommunityMemberResponse>>(`${this.url}/${communityId}`, { params });
  }

  public leaveCommunity(communityId: string) {
    return this.http.delete<void>(`${this.url}/${communityId}/leave`);
  }

  public removeMember(communityId: string, memberId: string) {
    return this.http.delete<void>(`${this.url}/${communityId}/remove/${memberId}`);
  }

  public updateMemberRole(communityId: string, memberId: string, role: string) {
    return this.http.patch<CommunityMemberResponse>(`${this.url}/${communityId}/role/${memberId}`, { role });
  }

  public banMember(communityId: string, memberId: string) {
    return this.http.post<void>(`${this.url}/${communityId}/ban/${memberId}`, {});
  }
}
