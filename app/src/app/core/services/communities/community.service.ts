import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { CommunityRequest, CommunityResponse, UpdateCommunityRequest } from '../../../models/communities/community.model';
import { TeamResponse } from '../../../models/teams/team.model';

@Injectable({
  providedIn: 'root',
})
export class CommunityService {

  api = API_URL;
  private url = `${this.api}/api/communities`;
  private http = inject(HttpClient);

  public createCommunity(request: CommunityRequest) {
    return this.http.post<CommunityResponse>(`${this.url}/create`, request);
  }

  public editCommunity(communityId: string, request: UpdateCommunityRequest) {
    return this.http.patch<CommunityResponse>(`${this.url}/edit/${communityId}`, request);
  }

  public detailCommunity(communityId: string) {
    return this.http.get<CommunityResponse>(`${this.url}/${communityId}`);
  }

  public deleteCommunity(communityId: string, deleteTeam: boolean = false) {
    const params = new HttpParams().set('deleteTeam', String(deleteTeam));
    return this.http.delete<void>(`${this.url}/${communityId}`, { params });
  }

  public detachTeam(communityId: string) {
    return this.http.delete<void>(`${this.url}/${communityId}/detach-team`);
  }

  public archiveCommunity(communityId: string) {
    return this.http.patch<void>(`${this.url}/${communityId}/archive`, null);
  }

  public listCommunities(name: string = '', page: number = 0, size: number = 20) {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (name.trim()) params = params.set('name', name.trim());
    return this.http.get<PageResponse<CommunityResponse>>(`${this.url}/list-communities`, { params });
  }

  /** Comunidades de um usuário qualquer — usado no perfil dele. */
  public listUserCommunities(userId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<CommunityResponse>>(`${this.url}/user/${userId}`, { params });
  }

  /** Comunidade vinculada a um Team; 404 quando o time não tem uma. */
  public detailCommunityByTeam(teamId: string) {
    return this.http.get<CommunityResponse>(`${this.url}/by-team/${teamId}`);
  }

  /** Comunidades fixadas pelo usuário na barra do feed (sem paginação: lista curta). */
  public listPinnedCommunities() {
    return this.http.get<CommunityResponse[]>(`${this.url}/pinned`);
  }

  public pinCommunity(communityId: string) {
    return this.http.post<CommunityResponse>(`${this.url}/pinned/${communityId}`, null);
  }

  /** Recebe a lista completa de fixadas na nova ordem. */
  public reorderPinnedCommunities(communityIds: string[]) {
    return this.http.put<CommunityResponse[]>(`${this.url}/pinned/order`, communityIds);
  }

  public unpinCommunity(communityId: string) {
    return this.http.delete<void>(`${this.url}/pinned/${communityId}`);
  }

  public listMyCommunities(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<CommunityResponse>>(`${this.url}/my-communities`, { params });
  }

  public attachTeam(communityId: string, teamId: string) {
    return this.http.post<CommunityResponse>(`${this.url}/${communityId}/attach-team/${teamId}`, {});
  }

  public listAvailableTeams(communityId: string) {
    return this.http.get<TeamResponse[]>(`${this.url}/${communityId}/available-teams`);
  }
}
