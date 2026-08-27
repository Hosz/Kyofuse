import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient } from '@angular/common/http';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { GamerProfileEditRequest } from '../../../models/profile/gamer-profile-edit-request.model';

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
}
