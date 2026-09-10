import { inject, Injectable, signal } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { NotificationResponse } from '../../../models/notifications/notification.model';
import { WebSocketService } from '../websocket/websocket.service';
import { Observable } from 'rxjs';

/** Tamanho de página usado só pra estimar a contagem de não lidas (não existe
 * endpoint de contagem no backend) — cobre bem a maioria dos casos práticos. */
const UNREAD_COUNT_SAMPLE_SIZE = 100;

@Injectable({
  providedIn: 'root',
})
export class NotificationService {

  api = API_URL;
  private url = `${this.api}/api/notifications`;
  private http = inject(HttpClient);
  private websocketService = inject(WebSocketService);

  /** Compartilhado entre a sidebar (badge) e a página de notificações. */
  readonly unreadCount = signal(0);

  /** Stream de notificações recebidas em tempo real via WebSocket */
  readonly newNotification$: Observable<NotificationResponse> =
    this.websocketService.watch<NotificationResponse>('/user/queue/notifications');

  constructor() {
    this.newNotification$.subscribe({
      next: (notification) => {
        if (notification.status === 'UNREAD') {
          this.unreadCount.update((count) => count + 1);
        }
      },
      error: (error) => console.error('[NotificationService] WebSocket notification error:', error),
    });
  }

  public listNotifications(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<NotificationResponse>>(`${this.url}`, { params });
  }

  /** Busca a contagem atômica de notificações não lidas diretamente do Redis via backend */
  public refreshUnreadCount(sync: boolean = false): void {
    const params = sync ? new HttpParams().set('sync', 'true') : undefined;
    this.http.get<{ unreadCount: number }>(`${this.url}/unread-count`, { params }).subscribe({
      next: (response) => {
        this.unreadCount.set(response.unreadCount ?? 0);
      },
      error: (error) => console.error('Failed to fetch unread notifications count:', error),
    });
  }

  public readNotification(notificationId: string) {
    return this.http.patch<void>(`${this.url}/${notificationId}/read`, null);
  }

  public archiveNotification(notificationId: string) {
    return this.http.patch<void>(`${this.url}/${notificationId}/archive`, null);
  }

  public readAllNotifications() {
    return this.http.patch<void>(`${this.url}/readall`, null);
  }

  public archiveAllNotifications() {
    return this.http.patch<void>(`${this.url}/archiveall`, null);
  }
}
