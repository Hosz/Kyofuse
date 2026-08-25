import { Injectable, signal } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { registerRequest } from '../../../models/auth/register-form.model';
import { loginRequest } from '../../../models/auth/login-form.model';
import { authResponse, RequestFail } from '../../../models/auth/auth-response.model';
import { Observable, catchError, finalize, map, of, shareReplay, tap } from 'rxjs';

/**
 * Access token e refresh token vivem em cookies HttpOnly (setados pela API): o JS nunca
 * os enxerga, então o estado "estou autenticado?" só pode ser conhecido de fato batendo
 * no backend (/me ou /refresh). O signal `authenticated` é só uma cache otimista em
 * memória, atualizada nesses pontos de contato.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {

  api = API_URL;
  private url = `${this.api}/api/auth`;
  private http = inject(HttpClient);

  private readonly authenticated = signal(false);

  private refreshInFlight: Observable<boolean> | null = null;

  public register(request: registerRequest): Observable<authResponse> {
    return this.http.post<authResponse>(`${this.url}/register`, request, { withCredentials: true })
      .pipe(tap(() => this.authenticated.set(true)));
  }

  public login(request: loginRequest): Observable<authResponse> {
    return this.http.post<authResponse>(`${this.url}/login`, request, { withCredentials: true })
      .pipe(tap(() => this.authenticated.set(true)));
  }

  public logout(): Observable<string | RequestFail> {
    return this.http.post<string | RequestFail>(`${this.url}/logout`, {}, { withCredentials: true })
      .pipe(tap(() => this.authenticated.set(false)));
  }

  /** Renova o access token via refresh token cookie. Deduplica chamadas concorrentes. */
  public refresh(): Observable<boolean> {
    if (this.refreshInFlight) {
      return this.refreshInFlight;
    }

    this.refreshInFlight = this.http.post(`${this.url}/refresh`, {}, { withCredentials: true }).pipe(
      map(() => {
        this.authenticated.set(true);
        return true;
      }),
      catchError(() => {
        this.authenticated.set(false);
        return of(false);
      }),
      finalize(() => this.refreshInFlight = null),
      shareReplay(1),
    );

    return this.refreshInFlight;
  }

  /** Confere a sessão contra o backend (usado nos guards, já que não há mais token local para decodificar). */
  public checkSession(): Observable<boolean> {
    return this.http.get(`${this.url}/me`, { withCredentials: true }).pipe(
      map(() => {
        this.authenticated.set(true);
        return true;
      }),
      catchError(() => {
        this.authenticated.set(false);
        return of(false);
      }),
    );
  }

  /** Estado conhecido em memória, sem chamar a API. Use checkSession() quando precisar de certeza. */
  public isAuthenticated(): boolean {
    return this.authenticated();
  }

  public clearSession(): void {
    this.authenticated.set(false);
  }
}
