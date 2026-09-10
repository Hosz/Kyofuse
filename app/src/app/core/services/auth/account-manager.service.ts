import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, forkJoin, of, tap } from 'rxjs';
import { API_URL } from '../../../models/api-url.model';
import { DisconnectAccountRequest, SavedAccount, SwitchAccountRequest, SwitchAccountResponse } from '../../../models/auth/multi-account.model';

@Injectable({
  providedIn: 'root',
})
export class AccountManagerService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/api/auth`;

  private readonly STORAGE_KEY = 'kyofuse_saved_accounts';
  private readonly DEVICE_ID_KEY = 'kyofuse_device_id';
  private readonly ACTIVE_USER_KEY = 'kyofuse_active_user_id';

  public readonly savedAccounts = signal<SavedAccount[]>(this.loadAccounts());
  public readonly activeUserId = signal<string | null>(this.loadActiveUserId());

  public getActiveUserId(): string | null {
    return this.activeUserId() ?? this.loadActiveUserId();
  }

  public setActiveUserId(userId: string | null): void {
    this.activeUserId.set(userId);
    try {
      if (userId) {
        localStorage.setItem(this.ACTIVE_USER_KEY, userId);
      } else {
        localStorage.removeItem(this.ACTIVE_USER_KEY);
      }
    } catch (e) {
      console.error('Failed to update active user id in localStorage:', e);
    }
  }

  public clearActiveUserId(): void {
    this.setActiveUserId(null);
  }

  private loadActiveUserId(): string | null {
    try {
      return localStorage.getItem(this.ACTIVE_USER_KEY);
    } catch {
      return null;
    }
  }

  public getDeviceId(): string {
    let deviceId = localStorage.getItem(this.DEVICE_ID_KEY);
    if (!deviceId) {
      deviceId = (typeof crypto !== 'undefined' && crypto.randomUUID)
        ? crypto.randomUUID()
        : 'dev_' + Math.random().toString(36).substring(2, 15);
      localStorage.setItem(this.DEVICE_ID_KEY, deviceId);
    }
    return deviceId;
  }

  public registerOrUpdateAccount(accountData: {
    userId: string;
    username: string;
    nickname?: string;
    email?: string;
    avatarUrl?: string;
    country?: string;
    showCountryFlag?: boolean;
    role?: string;
    switchToken?: string;
  }): void {
    this.setActiveUserId(accountData.userId);
    const list = this.loadAccounts();
    const existingIndex = list.findIndex(
      (a) =>
        a.userId === accountData.userId ||
        (a.username && accountData.username && a.username.toLowerCase() === accountData.username.toLowerCase())
    );

    const prevAccount = existingIndex >= 0 ? list[existingIndex] : undefined;

    const updatedAccount: SavedAccount = {
      userId: accountData.userId,
      username: accountData.username,
      nickname: accountData.nickname || prevAccount?.nickname || accountData.username,
      email: accountData.email || prevAccount?.email,
      avatarUrl: accountData.avatarUrl !== undefined ? accountData.avatarUrl : prevAccount?.avatarUrl,
      country: accountData.country !== undefined ? accountData.country : prevAccount?.country,
      showCountryFlag: accountData.showCountryFlag !== undefined ? accountData.showCountryFlag : prevAccount?.showCountryFlag,
      role: accountData.role || prevAccount?.role || 'USER',
      switchToken: accountData.switchToken || prevAccount?.switchToken,
      lastActiveAt: new Date().toISOString(),
    };

    if (existingIndex >= 0) {
      list[existingIndex] = updatedAccount;
    } else {
      list.push(updatedAccount);
    }

    this.saveAccounts(list);
  }

  public updateSwitchToken(userId: string, switchToken: string): void {
    const list = this.loadAccounts();
    const target = list.find((a) => a.userId === userId);
    if (target) {
      target.switchToken = switchToken;
      target.lastActiveAt = new Date().toISOString();
      this.saveAccounts(list);
    }
  }

  public switchAccount(targetUserId: string): Observable<SwitchAccountResponse> {
    const target = this.savedAccounts().find((a) => a.userId === targetUserId);
    if (!target || !target.switchToken) {
      throw new Error('Sessão da conta não encontrada localmente.');
    }

    const request: SwitchAccountRequest = {
      targetUserId: target.userId,
      switchToken: target.switchToken,
      deviceId: this.getDeviceId(),
    };

    return this.http.post<SwitchAccountResponse>(`${this.url}/switch-account`, request, { withCredentials: true })
      .pipe(
        tap((response) => {
          this.updateSwitchToken(response.userId, response.switchToken);
          this.registerOrUpdateAccount({
            userId: response.userId,
            username: response.username,
            email: response.email,
            role: response.role,
            switchToken: response.switchToken,
          });
        })
      );
  }

  public disconnectAccount(targetUserId: string): Observable<void> {
    const saved = this.loadAccounts().find((a) => a.userId === targetUserId);
    const request: DisconnectAccountRequest = {
      targetUserId,
      deviceId: this.getDeviceId(),
      switchToken: saved?.switchToken,
    };

    return this.http.post<void>(`${this.url}/disconnect-account`, request, { withCredentials: true })
      .pipe(
        tap(() => {
          this.removeLocalAccount(targetUserId);
        })
      );
  }

  public removeLocalAccount(targetUserId: string): void {
    const list = this.loadAccounts().filter((a) => a.userId !== targetUserId);
    if (this.activeUserId() === targetUserId) {
      this.clearActiveUserId();
    }
    this.saveAccounts(list);
  }

  public clearAllAccounts(): void {
    this.savedAccounts.set([]);
    this.clearActiveUserId();
    try {
      localStorage.removeItem(this.STORAGE_KEY);
      localStorage.removeItem(this.ACTIVE_USER_KEY);
    } catch (e) {
      console.error('Failed to clear accounts from localStorage:', e);
    }
  }

  public disconnectAllAccounts(): Observable<unknown> {
    const accounts = [...this.savedAccounts()];
    if (accounts.length === 0) {
      this.clearAllAccounts();
      return of(void 0);
    }
    const calls = accounts.map((acc) =>
      this.disconnectAccount(acc.userId).pipe(
        catchError(() => of(null))
      )
    );
    return forkJoin(calls).pipe(
      tap(() => {
        this.clearAllAccounts();
      })
    );
  }

  public requestCurrentSwitchToken(): Observable<{ switchToken: string }> {
    return this.http.post<{ switchToken: string }>(
      `${this.url}/switch-token`,
      {},
      {
        headers: { 'X-Device-Id': this.getDeviceId() },
        withCredentials: true,
      }
    );
  }

  private loadAccounts(): SavedAccount[] {
    try {
      const data = localStorage.getItem(this.STORAGE_KEY);
      if (!data) return [];
      const list: SavedAccount[] = JSON.parse(data);
      if (!Array.isArray(list)) return [];

      const uniqueList: SavedAccount[] = [];
      for (const acc of list) {
        if (!acc || !acc.userId || !acc.username) continue;
        const existingIndex = uniqueList.findIndex(
          (u) =>
            u.userId === acc.userId ||
            (u.username && acc.username && u.username.toLowerCase() === acc.username.toLowerCase())
        );
        if (existingIndex >= 0) {
          uniqueList[existingIndex] = {
            ...uniqueList[existingIndex],
            ...acc,
            switchToken: acc.switchToken || uniqueList[existingIndex].switchToken,
          };
        } else {
          uniqueList.push(acc);
        }
      }
      return uniqueList;
    } catch {
      return [];
    }
  }

  private saveAccounts(accounts: SavedAccount[]): void {
    this.savedAccounts.set([...accounts]);
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(accounts));
    } catch (e) {
      console.error('Failed to save accounts in localStorage:', e);
    }
  }
}
