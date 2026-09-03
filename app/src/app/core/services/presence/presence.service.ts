import { effect, inject, Injectable, OnDestroy, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '../../../models/api-url.model';
import { BatchPresenceRequest, PresenceResponse, PresenceStatus } from '../../../models/presence/presence.model';
import { WebSocketService } from '../websocket/websocket.service';
import { AuthService } from '../auth/auth.service';
import { Observable, Subscription } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PresenceService implements OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly websocketService = inject(WebSocketService);
  private readonly authService = inject(AuthService);

  private readonly url = `${API_URL}/api/presence`;

  /** Mapa reativo de presença por userId */
  readonly presenceMap = signal<Map<string, PresenceResponse>>(new Map());

  private wsSubscription?: Subscription;
  private heartbeatInterval?: any;

  constructor() {
    effect(() => {
      const isAuth = this.authService.isAuthenticated();
      if (isAuth) {
        this.startPresenceTracking();
      } else {
        this.stopPresenceTracking();
      }
    });
  }

  private visibilityListener = (): void => {
    if (document.visibilityState === 'visible') {
      this.sendHeartbeat();
    }
  };

  private startPresenceTracking(): void {
    // 1. Escuta atualizações de presença em tempo real via WebSocket
    this.wsSubscription = this.websocketService
      .watch<PresenceResponse>('/topic/presence')
      .subscribe({
        next: (presence) => {
          this.updateUserPresence(presence);
        },
        error: (err) => console.error('[PresenceService] WebSocket error:', err),
      });

    // 2. Inicia heartbeat a cada 45 segundos
    this.sendHeartbeat();
    if (!this.heartbeatInterval) {
      this.heartbeatInterval = setInterval(() => {
        if (document.visibilityState === 'visible') {
          this.sendHeartbeat();
        }
      }, 45000);
    }
    if (typeof document !== 'undefined') {
      document.addEventListener('visibilitychange', this.visibilityListener);
    }
  }

  private stopPresenceTracking(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
      this.wsSubscription = undefined;
    }
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = undefined;
    }
    if (typeof document !== 'undefined') {
      document.removeEventListener('visibilitychange', this.visibilityListener);
    }
  }

  public sendHeartbeat(): void {
    if (!this.authService.isAuthenticated()) return;
    this.http.post<void>(`${this.url}/heartbeat`, null).subscribe({
      error: (err) => console.debug('[PresenceService] Heartbeat error:', err),
    });
    this.websocketService.publish('/app/presence/heartbeat', {});
  }

  public fetchUserPresence(userId: string): Observable<PresenceResponse> {
    return this.http.get<PresenceResponse>(`${this.url}/${userId}`);
  }

  public fetchBatchPresence(userIds: string[]): void {
    if (!userIds || userIds.length === 0) return;
    const uniqueIds = Array.from(new Set(userIds));
    this.http.post<Record<string, PresenceResponse> | PresenceResponse[]>(`${this.url}/batch`, { userIds: uniqueIds } as BatchPresenceRequest)
      .subscribe({
        next: (presences) => {
          this.presenceMap.update((map) => {
            const newMap = new Map(map);
            const list: PresenceResponse[] = Array.isArray(presences)
              ? presences
              : Object.values(presences || {});
            for (const p of list) {
              if (p && p.userId) {
                newMap.set(p.userId, p);
              }
            }
            return newMap;
          });
        },
        error: (err) => console.error('[PresenceService] Batch fetch error:', err),
      });
  }

  public isUserOnline(userId: string): boolean {
    const presence = this.presenceMap().get(userId);
    return presence?.status === 'ONLINE';
  }

  public getUserStatus(userId: string): PresenceStatus {
    const presence = this.presenceMap().get(userId);
    return presence?.status ?? 'OFFLINE';
  }

  public getUserLastSeen(userId: string): string | null {
    const presence = this.presenceMap().get(userId);
    return presence?.lastSeen ?? null;
  }

  private updateUserPresence(presence: PresenceResponse): void {
    this.presenceMap.update((map) => {
      const newMap = new Map(map);
      newMap.set(presence.userId, presence);
      return newMap;
    });
  }

  ngOnDestroy(): void {
    this.stopPresenceTracking();
  }
}
