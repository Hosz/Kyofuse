import { Cs2Map, PlayerRole, Playstyle } from '../../shared/models/profile-options.model';

export interface GamerProfileEditRequest {
  nickname?: string;
  bio?: string;
  avatarUrl?: string;
  bannerUrl?: string;
  country?: string;
  city?: string;
  state?: string;
  mainRole?: PlayerRole | null;
  secondaryRole?: PlayerRole | null;
  premierRating?: number | null;
  faceitLevel?: number | null;
  gcRank?: number | null;
  playstyle?: Playstyle | null;
  lookingForTeam?: boolean;
  lookingForDuo?: boolean;
  favoriteMaps?: Cs2Map[];
}
