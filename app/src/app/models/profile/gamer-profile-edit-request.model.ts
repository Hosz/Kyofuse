import { Cs2Map, PlayerRole, Playstyle } from '../../shared/models/profile-options.model';

export interface GamerProfileEditRequest {
  nickname?: string;
  bio?: string;
  avatarUrl?: string;
  bannerUrl?: string;
  country?: string;
  city?: string;
  state?: string;
  mainRole?: PlayerRole;
  secondaryRole?: PlayerRole;
  premierRating?: number;
  faceitLevel?: number;
  gcRank?: number;
  playstyle?: Playstyle;
  lookingForTeam?: boolean;
  lookingForDuo?: boolean;
  favoriteMaps?: Cs2Map[];
}
