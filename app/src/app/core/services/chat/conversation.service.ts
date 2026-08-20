import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { ConversationRequest, ConversationResponse, UpdateConversationRequest } from '../../../models/chat/chat.model';

@Injectable({
  providedIn: 'root',
})
export class ConversationService {

  api = API_URL;
  private url = `${this.api}/api/conversations`;
  private http = inject(HttpClient);

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
