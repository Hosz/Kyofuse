import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { gamerProfileResponse, ProfileFilter } from '../../../models/profile/gamer-profile.model';
import { GamerProfileEditRequest } from '../../../models/profile/gamer-profile-edit-request.model';
import { PageResponse } from '../../../models/page-response.model';

@Injectable({
  providedIn: 'root',
})
export class ProfileService {

  api = API_URL;
  private url = `${this.api}/api/profile`;
  private http = inject(HttpClient);

  public myProfile() {
    return this.http.get<gamerProfileResponse>(`${this.url}/me`);
  }

  public userProfile(userId: string) {
    return this.http.get<gamerProfileResponse>(`${this.url}/${userId}`);
  }

  public editProfile(profileData: GamerProfileEditRequest) {
    return this.http.patch<gamerProfileResponse>(`${this.url}/edit`, profileData);
  }


  public listingProfiles(filter: ProfileFilter, page: number = 0, size: number = 20) {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    for (const [key, value] of Object.entries(filter)) {
      if (value === undefined || value === null) continue;
      params = Array.isArray(value)
        ? value.reduce((acc, item) => acc.append(key, String(item)), params)
        : params.set(key, String(value));
    }
    
    return this.http.get<PageResponse<gamerProfileResponse>>(`${this.url}`, { params });
  }

  public getMyAnalytics() {
    return this.http.get<{ profileId: string; dailyUniqueVisitors: number; monthlyUniqueVisitors: number }>(`${this.url}/me/analytics`);
  }
}
