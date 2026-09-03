import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { MessageInfoResponse, MessageRequest, MessageResponse, MessageStatusEvent } from '../../../models/chat/chat.model';
import { TypingEvent } from '../../../shared/models/chat.model';
import { WebSocketService } from '../websocket/websocket.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MessageService {

  api = API_URL;
  private url = `${this.api}/api/messages`;
  private http = inject(HttpClient);
  private websocketService = inject(WebSocketService);

  /**
   * Stream de atualizações de status de mensagem em tempo real (DELIVERED, READ).
   */
  readonly messageStatus$: Observable<MessageStatusEvent> =
    this.websocketService.watch<MessageStatusEvent>('/user/queue/message-status');

  /**
   * Assina as mensagens em tempo real de uma conversa via WebSocket.
   */
  public watchConversation(conversationId: string): Observable<MessageResponse> {
    return this.websocketService.watch<MessageResponse>(`/topic/conversations/${conversationId}`);
  }

  /**
   * Assina eventos de "está digitando..." de uma conversa em tempo real.
   */
  public watchTyping(conversationId: string): Observable<TypingEvent> {
    return this.websocketService.watch<TypingEvent>(`/topic/conversations/${conversationId}/typing`);
  }

  /**
   * Envia evento de typing via WebSocket STOMP (e fallback HTTP).
   */
  public sendTyping(conversationId: string, isTyping: boolean): void {
    this.websocketService.publish(`/app/chat/${conversationId}/typing`, { isTyping });
  }

  public sendMessage(conversationId: string, request: MessageRequest) {
    return this.http.post<MessageResponse>(`${this.url}/${conversationId}/send-message`, request);
  }

  /** sort=createdAt,desc traz as mais recentes primeiro (a página que interessa numa
   * conversa longa) — o chamador inverte pra ordem cronológica antes de exibir. */
  public getMessages(conversationId: string, page: number = 0, size: number = 50) {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');
    return this.http.get<PageResponse<MessageResponse>>(`${this.url}/${conversationId}/messages`, { params });
  }

  public deleteMessage(conversationId: string, messageId: string) {
    return this.http.delete<void>(`${this.url}/${conversationId}/delete/${messageId}`);
  }

  public markAsRead(conversationId: string): Observable<void> {
    return this.http.post<void>(`${this.url}/${conversationId}/read`, {});
  }

  public markAsDelivered(messageIds: string[]): Observable<void> {
    return this.http.post<void>(`${this.url}/delivered`, messageIds);
  }

  public getMessageInfo(conversationId: string, messageId: string): Observable<MessageInfoResponse> {
    return this.http.get<MessageInfoResponse>(`${this.url}/${conversationId}/info/${messageId}`);
  }
}
