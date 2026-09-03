import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { API_URL } from '../../../models/api-url.model';
import { LeaderboardEntryResponse, UserRankResponse } from '../../../models/leaderboard/leaderboard.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LeaderboardService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/api/v1/leaderboard`;

  public getTopPlayers(limit: number = 50): Observable<LeaderboardEntryResponse[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<LeaderboardEntryResponse[]>(`${this.url}/top`, { params });
  }

  public getMyRank(): Observable<UserRankResponse> {
    return this.http.get<UserRankResponse>(`${this.url}/rank/me`);
  }

  public getUserRank(userId: string): Observable<UserRankResponse> {
    return this.http.get<UserRankResponse>(`${this.url}/rank/${userId}`);
  }

  public getAroundMe(range: number = 3): Observable<LeaderboardEntryResponse[]> {
    const params = new HttpParams().set('range', range.toString());
    return this.http.get<LeaderboardEntryResponse[]>(`${this.url}/around/me`, { params });
  }
}
