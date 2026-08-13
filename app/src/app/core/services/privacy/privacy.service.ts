import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient } from '@angular/common/http';
import { UserPrivacySettingsRequest, UserPrivacySettingsResponse } from '../../../models/privacy/privacy.model';

@Injectable({
  providedIn: 'root',
})
export class PrivacyService {

  api = API_URL;
  private url = `${this.api}/api/user-privacy-settings`
  private http = inject(HttpClient);

  public setSettings(request: UserPrivacySettingsRequest) {
    return this.http.put<UserPrivacySettingsResponse>(`${this.url}/set`, request);
  }

  public getSettings() {
    return this.http.get<UserPrivacySettingsResponse>(`${this.url}/get`);
  }
}
