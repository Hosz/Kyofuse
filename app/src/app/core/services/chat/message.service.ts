import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { MessageRequest, MessageResponse } from '../../../models/chat/chat.model';

@Injectable({
  providedIn: 'root',
})
export class MessageService {

  api = API_URL;
  private url = `${this.api}/api/messages`;
  private http = inject(HttpClient);

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
}
