import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../models/api-url.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageResponse } from '../../../models/page-response.model';
import { UserBlockResponse } from '../../../models/block/block.model';

@Injectable({
  providedIn: 'root',
})
export class BlockService {

  api = API_URL;
  private url = `${this.api}/api/user-block`
  private http = inject(HttpClient);

  public blockUser(userId: string) {
    return this.http.post<UserBlockResponse>(`${this.url}/${userId}/block`, null);
  }

  public getBlockedUsers(page: number = 0, size: number = 20) {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<UserBlockResponse>>(`${this.url}/blocked-users`, { params });
  }

  public unblockUser(userId: string) {
    return this.http.delete<void>(`${this.url}/unblock/${userId}`);
  }
}
