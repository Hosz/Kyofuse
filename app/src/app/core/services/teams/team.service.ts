import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { TeamFilter, TeamRequest, TeamResponse, UpdateTeamRequest, UpdateTeamRequiredRolesRequest } from '../../../models/teams/team.model';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';

@Injectable({
  providedIn: 'root',
})
export class TeamService {

  api = API_URL;
  private url = `${this.api}/api/teams`;
  private http = inject(HttpClient);

  public createTeams(request: TeamRequest) {
    return this.http.post<TeamResponse>(`${this.url}/create`, request);
  }

  public detailTeam(teamId: string) {
    return this.http.get<TeamResponse>(`${this.url}/${teamId}`);
  }

  public listingTeams(filter: TeamFilter, page: number = 0, size: number = 20) {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    for (const [key, value] of Object.entries(filter)) {
      if (value === undefined || value === null) continue;
      params = Array.isArray(value)
        ? value.reduce((acc, item) => acc.append(key, String(item)), params)
        : params.set(key, String(value));
    }

    return this.http.get<PageResponse<TeamResponse>>(`${this.url}`, { params });
  }

  /** Jogadores que marcaram "procurando time" no perfil. Só o dono do time pode listar. */
  public listPlayersLookingForTeam(teamId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<gamerProfileResponse>>(`${this.url}/${teamId}/looking-for-team`, { params });
  }

  public listingMyTeams(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<TeamResponse>>(`${this.url}/my-teams`, { params });
  }

  /** Times de um usuário qualquer — usado no perfil dele. */
  public listingUserTeams(userId: string, page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<TeamResponse>>(`${this.url}/user/${userId}`, { params });
  }

  public editTeam(teamId: string, request: UpdateTeamRequest) {
    return this.http.patch<TeamResponse>(`${this.url}/edit/${teamId}`, request);
  }

  public inactiveTeam(teamId: string) {
    return this.http.patch<TeamResponse>(`${this.url}/${teamId}/inactivate`, null);
  }

  public manageRequiredRoles(teamId: string, request: UpdateTeamRequiredRolesRequest) {
    return this.http.put<TeamResponse>(`${this.url}/${teamId}/required-roles`, request);
  }
}
