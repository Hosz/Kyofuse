import { Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { registerRequest } from '../../../models/auth/register-form.model';
import { loginRequest } from '../../../models/auth/login-form.model';
import { authResponse, RequestFail } from '../../../models/auth/auth-response.model';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  api = API_URL;
  private url = `${this.api}/api/auth`;
  private http = inject(HttpClient);
  private jwtHelper = new JwtHelperService();

  public register(request: registerRequest) {
    return this.http.post<authResponse>(`${this.url}/register`, request);
  }

  public login(request: loginRequest) {
    return this.http.post<authResponse>(`${this.url}/login`, request);
  }

  public logout(): Observable<string | RequestFail> {
    return this.http.post<string | RequestFail>(`${this.url}/logout`, {});
  }

  public saveToken(token: string): void {
    localStorage.setItem("accessToken", token)
  }

  public clearToken() {
    localStorage.removeItem("accessToken");
  }

  public getToken(): string {
    return localStorage.getItem("accessToken") ?? ""
  }

  public isAuthenticated(): boolean {
    const token = this.getToken();

    if (!token) {
      return false;
    }

    if (this.jwtHelper.isTokenExpired(token)) {
      this.clearToken();
      return false;
    }

    return true;
  }
}
