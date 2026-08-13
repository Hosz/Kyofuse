import { inject, Injectable, signal } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { NotificationResponse } from '../../../models/notifications/notification.model';

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

  /** Compartilhado entre a sidebar (badge) e a página de notificações. */
  readonly unreadCount = signal(0);

  public listNotifications(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<NotificationResponse>>(`${this.url}`, { params });
  }

  /** Não existe endpoint de contagem — busca uma página grande e conta as não lidas nela. */
  public refreshUnreadCount(): void {
    this.listNotifications(0, UNREAD_COUNT_SAMPLE_SIZE).subscribe({
      next: (response) => {
        this.unreadCount.set(response.content.filter((n) => n.status === 'UNREAD').length);
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
}
