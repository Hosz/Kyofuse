import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../../../models/api-url.model';
import { UserSession } from '../../../models/auth/user-session.model';

@Injectable({
  providedIn: 'root',
})
export class UserSessionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/api/auth/sessions`;

  /** Controla exibição do modal/banner "Confiar neste dispositivo?" após login */
  readonly showTrustPrompt = signal(false);

  public listSessions(): Observable<UserSession[]> {
    return this.http.get<UserSession[]>(this.url);
  }

  public trustCurrentDevice(): Observable<void> {
    return this.http.patch<void>(`${this.url}/trust`, null);
  }

  public untrustDevice(sessionId: string): Observable<void> {
    return this.http.patch<void>(`${this.url}/${sessionId}/untrust`, null);
  }

  public revokeSession(sessionId: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${sessionId}`);
  }

  public revokeAllOtherSessions(): Observable<void> {
    return this.http.delete<void>(`${this.url}/others`);
  }
}
