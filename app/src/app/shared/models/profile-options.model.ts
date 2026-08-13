export type PlayerRole = 'ENTRY_FRAGGER' | 'AWPER' | 'IGL' | 'SUPPORT' | 'LURKER' | 'RIFLER' | 'FLEX';
export type Playstyle = 'CASUAL' | 'COMPETITIVE' | 'TRYHARD' | 'TEAM_ORIENTED' | 'SOLO_QUEUE';
export type Cs2Map =
  | 'ANCIENT'
  | 'ANUBIS'
  | 'ALPINE'
  | 'CACHE'
  | 'DUST2'
  | 'INFERNO'
  | 'ITALY'
  | 'MIRAGE'
  | 'NUKE'
  | 'OFFICE'
  | 'OVERPASS'
  | 'STRONGHOLD'
  | 'TRAIN'
  | 'VERTIGO'
  | 'WARDEN';

/** Espelham os enums do backend (profiles/enums/*.java). */
export const PLAYER_ROLE_OPTIONS: { value: PlayerRole; label: string }[] = [
  { value: 'ENTRY_FRAGGER', label: 'Entry Fragger' },
  { value: 'AWPER', label: 'AWPer' },
  { value: 'IGL', label: 'IGL' },
  { value: 'SUPPORT', label: 'Suporte' },
  { value: 'LURKER', label: 'Lurker' },
  { value: 'RIFLER', label: 'Rifler' },
  { value: 'FLEX', label: 'Flex' },
];

export const PLAYSTYLE_OPTIONS: { value: Playstyle; label: string }[] = [
  { value: 'CASUAL', label: 'Casual' },
  { value: 'COMPETITIVE', label: 'Competitivo' },
  { value: 'TRYHARD', label: 'Tryhard' },
  { value: 'TEAM_ORIENTED', label: 'Focado em time' },
  { value: 'SOLO_QUEUE', label: 'Solo queue' },
];

export const CS2_MAP_OPTIONS: { value: Cs2Map; label: string }[] = [
  { value: 'ANCIENT', label: 'Ancient' },
  { value: 'ANUBIS', label: 'Anubis' },
  { value: 'ALPINE', label: 'Alpine' },
  { value: 'CACHE', label: 'Cache' },
  { value: 'DUST2', label: 'Dust II' },
  { value: 'INFERNO', label: 'Inferno' },
  { value: 'ITALY', label: 'Italy' },
  { value: 'MIRAGE', label: 'Mirage' },
  { value: 'NUKE', label: 'Nuke' },
  { value: 'OFFICE', label: 'Office' },
  { value: 'OVERPASS', label: 'Overpass' },
  { value: 'STRONGHOLD', label: 'Stronghold' },
  { value: 'TRAIN', label: 'Train' },
  { value: 'VERTIGO', label: 'Vertigo' },
  { value: 'WARDEN', label: 'Warden' },
];
