import { Injectable, signal } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { registerRequest } from '../../../models/auth/register-form.model';
import { loginRequest } from '../../../models/auth/login-form.model';
import { authMeResponse, authResponse, isMfaRequired, loginResult, RequestFail } from '../../../models/auth/auth-response.model';
import { mfaVerifyRequest } from '../../../models/auth/mfa.model';
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
  private logoutInProgress = false;

  private refreshInFlight: Observable<boolean> | null = null;

  public register(request: registerRequest): Observable<authResponse> {
    return this.http.post<authResponse>(`${this.url}/register`, request, { withCredentials: true })
      .pipe(tap(() => this.authenticated.set(true)));
  }

  /** Se a conta tiver 2FA ativo, o backend responde com um desafio (mfaRequired) em vez
   * de autenticar de vez — os cookies de sessão só são setados depois de verifyMfa(). */
  public login(request: loginRequest): Observable<loginResult> {
    return this.http.post<loginResult>(`${this.url}/login`, request, { withCredentials: true })
      .pipe(tap((result) => {
        if (!isMfaRequired(result)) {
          this.authenticated.set(true);
        }
      }));
  }

  public verifyMfa(request: mfaVerifyRequest): Observable<authResponse> {
    return this.http.post<authResponse>(`${this.url}/2fa/verify`, request, { withCredentials: true })
      .pipe(tap(() => this.authenticated.set(true)));
  }

  public logout(): Observable<void> {
    this.logoutInProgress = true;
    return this.http.post<void>(`${this.url}/logout`, {}, { withCredentials: true })
      .pipe(
        tap(() => this.authenticated.set(false)),
        finalize(() => this.logoutInProgress = false)
      );
  }

  /**
   * Renova o access token via refresh token cookie. Deduplica chamadas concorrentes.
   * Diferencia entre 400 (token ausente = erro técnico) e 401 (token expirado = recuperável).
   */
  public refresh(): Observable<boolean> {
    if (this.refreshInFlight) {
      return this.refreshInFlight;
    }

    this.refreshInFlight = this.http.post(`${this.url}/refresh`, {}, { withCredentials: true }).pipe(
      map(() => {
        this.authenticated.set(true);
        return true;
      }),
      catchError((error: HttpErrorResponse) => {
        this.authenticated.set(false);

        // ⚠️ NOVO: Diferenciar 400 (ausente) de 401 (expirado)
        if (error.status === 400) {
          // Token ausente = erro técnico, não pode recuperar
          console.debug('Auth: Refresh token ausente (400)');
          return of(false);
        }

        if (error.status === 401) {
          // Token expirado/revogado = erro legítimo, pode tentar depois
          console.debug('Auth: Refresh token expirado/revogado (401)');
          return of(false);
        }

        // Outro erro = retorna false
        console.debug('Auth: Erro desconhecido no refresh', error.status);
        return of(false);
      }),
      finalize(() => this.refreshInFlight = null),
      shareReplay(1),
    );

    return this.refreshInFlight;
  }

  /**
   * Verifica se logout está em progresso para evitar requisições concorrentes.
   */
  public isLogoutInProgress(): boolean {
    return this.logoutInProgress;
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

  /** Igual ao /me usado em checkSession(), mas expondo o corpo (ex: totpEnabled) pra telas de conta/segurança. */
  public me(): Observable<authMeResponse> {
    return this.http.get<authMeResponse>(`${this.url}/me`, { withCredentials: true });
  }

  /** Estado conhecido em memória, sem chamar a API. Use checkSession() quando precisar de certeza. */
  public isAuthenticated(): boolean {
    return this.authenticated();
  }

  public clearSession(): void {
    this.authenticated.set(false);
  }
}
