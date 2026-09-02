import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../../../models/api-url.model';
import { MediaUploadResponse } from '../../../models/media/media-upload-response.model';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { TeamResponse } from '../../../models/teams/team.model';
import { CommunityResponse } from '../../../models/communities/community.model';

@Injectable({
  providedIn: 'root',
})
export class MediaService {
  private http = inject(HttpClient);
  private apiUrl = API_URL;

  public uploadImage(file: File): Observable<MediaUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<MediaUploadResponse>(`${this.apiUrl}/api/media/upload`, formData);
  }

  public uploadUserAvatar(file: File): Observable<gamerProfileResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<gamerProfileResponse>(`${this.apiUrl}/api/profile/me/avatar`, formData);
  }

  public uploadUserBanner(file: File): Observable<gamerProfileResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<gamerProfileResponse>(`${this.apiUrl}/api/profile/me/banner`, formData);
  }

  public uploadTeamAvatar(teamId: string, file: File): Observable<TeamResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<TeamResponse>(`${this.apiUrl}/api/teams/${teamId}/avatar`, formData);
  }

  public uploadTeamBanner(teamId: string, file: File): Observable<TeamResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<TeamResponse>(`${this.apiUrl}/api/teams/${teamId}/banner`, formData);
  }

  public uploadCommunityAvatar(communityId: string, file: File): Observable<CommunityResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<CommunityResponse>(`${this.apiUrl}/api/communities/${communityId}/avatar`, formData);
  }

  public uploadCommunityBanner(communityId: string, file: File): Observable<CommunityResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<CommunityResponse>(`${this.apiUrl}/api/communities/${communityId}/banner`, formData);
  }
}
