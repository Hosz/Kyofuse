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

  /** Controla exibição do modal/banner "Confiar neste dispositivo?" após carregar o feed */
  readonly showTrustPrompt = signal(false);
  private readonly PENDING_TRUST_KEY = 'kyofuse_pending_trust_prompt';

  public markTrustPromptPending(): void {
    try {
      localStorage.setItem(this.PENDING_TRUST_KEY, 'true');
    } catch {}
  }

  public checkAndTriggerTrustPrompt(): void {
    try {
      const pending = localStorage.getItem(this.PENDING_TRUST_KEY);
      if (pending === 'true') {
        localStorage.removeItem(this.PENDING_TRUST_KEY);
        setTimeout(() => {
          this.showTrustPrompt.set(true);
        }, 700);
      }
    } catch {}
  }

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
