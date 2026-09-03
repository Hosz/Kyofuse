export interface LeaderboardEntryResponse {
  rank: number;
  userId: string;
  nickname: string;
  avatarUrl: string | null;
  country: string | null;
  score: number;
}

export interface UserRankResponse {
  userId: string;
  rank: number | null;
  score: number;
  totalPlayers: number;
}
