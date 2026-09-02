import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../../../models/api-url.model';
import {
  ChangePasswordRequest,
  UpdateEmailRequest,
  UpdateUsernameRequest,
  UserAccountResponse,
} from '../../../models/account/user-account.model';

@Injectable({
  providedIn: 'root',
})
export class UserAccountService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_URL}/api/users/me`;

  getAccount(): Observable<UserAccountResponse> {
    return this.http.get<UserAccountResponse>(`${this.baseUrl}/account`);
  }

  updateUsername(request: UpdateUsernameRequest): Observable<UserAccountResponse> {
    return this.http.patch<UserAccountResponse>(`${this.baseUrl}/username`, request);
  }

  updateEmail(request: UpdateEmailRequest): Observable<UserAccountResponse> {
    return this.http.patch<UserAccountResponse>(`${this.baseUrl}/email`, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/password`, request);
  }

  linkGoogle(idToken: string): Observable<UserAccountResponse> {
    return this.http.post<UserAccountResponse>(`${this.baseUrl}/social/google`, { idToken });
  }

  unlinkGoogle(): Observable<UserAccountResponse> {
    return this.http.delete<UserAccountResponse>(`${this.baseUrl}/social/google`);
  }

  linkSteam(openIdParams: Record<string, string>): Observable<UserAccountResponse> {
    return this.http.post<UserAccountResponse>(`${this.baseUrl}/social/steam`, openIdParams);
  }

  unlinkSteam(): Observable<UserAccountResponse> {
    return this.http.delete<UserAccountResponse>(`${this.baseUrl}/social/steam`);
  }

  deactivateAccount(request: import('../../../models/account/user-account.model').DeactivateAccountRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/deactivate`, request);
  }

  scheduleDeletion(request: import('../../../models/account/user-account.model').ScheduleDeletionRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/schedule-deletion`, request);
  }
}
