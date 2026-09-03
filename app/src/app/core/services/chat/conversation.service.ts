import { computed, inject, Injectable, signal } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { PageResponse } from '../../../models/page-response.model';
import { ConversationRequest, ConversationResponse, MessageResponse, UpdateConversationRequest } from '../../../models/chat/chat.model';
import { WebSocketService } from '../websocket/websocket.service';
import { CurrentUserService } from '../profile/current-user.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ConversationService {

  api = API_URL;
  private url = `${this.api}/api/conversations`;
  private http = inject(HttpClient);
  private websocketService = inject(WebSocketService);
  private router = inject(Router);
  private currentUserService = inject(CurrentUserService);

  /** Compartilhado entre a sidebar (badge de notificação na aba conversas) e o dock. */
  readonly unreadCount = signal(0);
  readonly hasUnread = computed(() => this.unreadCount() > 0);

  /** Stream de mensagens recebidas em tempo real via WebSocket */
  readonly newMessage$: Observable<MessageResponse> =
    this.websocketService.watch<MessageResponse>('/user/queue/messages');

  constructor() {
    this.newMessage$.subscribe({
      next: (message) => {
        const myId = this.currentUserService.userId();
        if (!myId || message.senderId !== myId) {
          this.unreadCount.update((count) => count + 1);
        }
      },
      error: (error) => console.error('[ConversationService] WebSocket message error:', error),
    });
  }

  public refreshUnreadCount(): void {
    this.http.get<{ unreadCount: number }>(`${this.url}/unread-count`).subscribe({
      next: (response) => {
        this.unreadCount.set(response.unreadCount ?? 0);
      },
      error: (error) => console.error('Failed to fetch unread conversations count:', error),
    });
  }

  public refreshUnreadStatus(): void {
    this.refreshUnreadCount();
  }

  public decrementUnread(amount: number = 1): void {
    if (amount <= 0) return;
    this.unreadCount.update((count) => Math.max(0, count - amount));
  }

  public incrementUnread(amount: number = 1): void {
    if (amount <= 0) return;
    this.unreadCount.update((count) => count + amount);
  }

  public setUnreadCount(total: number): void {
    this.unreadCount.set(Math.max(0, total));
  }

  public markAsRead(): void {
    // Mantido por compatibilidade
  }

  public createConversation(request: ConversationRequest) {
    return this.http.post<ConversationResponse>(`${this.url}/create`, request);
  }

  /** Só conversas GROUP são editáveis, e apenas por um ADMIN ativo do grupo. */
  public editGroupConversation(conversationId: string, request: UpdateConversationRequest) {
    return this.http.patch<ConversationResponse>(`${this.url}/edit/${conversationId}`, request);
  }

  public acceptDirectConversation(conversationId: string) {
    return this.http.patch<ConversationResponse>(`${this.url}/${conversationId}/accept`, null);
  }

  public declineDirectConversation(conversationId: string) {
    return this.http.patch<ConversationResponse>(`${this.url}/${conversationId}/decline`, null);
  }

  public revokeDirectConversationPermission(conversationId: string) {
    return this.http.patch<ConversationResponse>(`${this.url}/${conversationId}/revoke`, null);
  }

  public allowDirectConversationPermission(conversationId: string) {
    return this.http.patch<ConversationResponse>(`${this.url}/${conversationId}/allow`, null);
  }

  public listDirectConversations(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<ConversationResponse>>(`${this.url}/list-direct-conversations`, { params });
  }

  public listGroupConversations(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<ConversationResponse>>(`${this.url}/list-group-conversations`, { params });
  }

  public listCommunityConversations(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<ConversationResponse>>(`${this.url}/list-community-conversation`, { params });
  }

  public getConversationDetails(conversationId: string) {
    return this.http.get<ConversationResponse>(`${this.url}/${conversationId}/details`);
  }
}
