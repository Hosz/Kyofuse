import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../../../models/api-url.model';
import { disable2faRequest, totpConfirmResponse, totpSetupResponse } from '../../../models/auth/mfa.model';

/** Gerencia o ciclo de vida do 2FA de uma conta já autenticada (ativar/confirmar/desativar).
 * O passo de verificação no login (com o mfaToken) fica em AuthService, junto do resto do auth. */
@Injectable({
  providedIn: 'root',
})
export class TwoFactorAuthService {

  private api = API_URL;
  private url = `${this.api}/api/auth/2fa`;
  private http = inject(HttpClient);

  public setup(): Observable<totpSetupResponse> {
    return this.http.post<totpSetupResponse>(`${this.url}/setup`, {}, { withCredentials: true });
  }

  public confirm(code: string): Observable<totpConfirmResponse> {
    return this.http.post<totpConfirmResponse>(`${this.url}/confirm`, { code }, { withCredentials: true });
  }

  public disable(password: string): Observable<void> {
    const request: disable2faRequest = { password };
    return this.http.post<void>(`${this.url}/disable`, request, { withCredentials: true });
  }
}
