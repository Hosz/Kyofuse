import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { ConversationMemberResponse } from '../../../models/chat/chat.model';

@Injectable({
  providedIn: 'root',
})
export class ConversationMemberService {

  api = API_URL;
  private url = `${this.api}/api/conversations/members`;
  private http = inject(HttpClient);

  public listConversationMembers(conversationId: string, page: number = 0, size: number = 50) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<ConversationMemberResponse>>(`${this.url}/${conversationId}/list-members`, { params });
  }

  public addMember(conversationId: string, memberId: string) {
    return this.http.post<void>(`${this.url}/${conversationId}/add/${memberId}`, null);
  }

  public promoteMemberToAdmin(conversationId: string, memberId: string) {
    return this.http.patch<void>(`${this.url}/${conversationId}/promote/${memberId}`, null);
  }

  public demoteAdminToMember(conversationId: string, memberId: string) {
    return this.http.patch<void>(`${this.url}/${conversationId}/demote/${memberId}`, null);
  }

  public removeMember(conversationId: string, memberId: string) {
    return this.http.delete<void>(`${this.url}/${conversationId}/remove/${memberId}`);
  }

  public leaveConversation(conversationId: string) {
    return this.http.delete<void>(`${this.url}/${conversationId}/leave`);
  }
}
