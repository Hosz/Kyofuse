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
export const PLAYER_ROLE_OPTIONS: { value: PlayerRole; label: string; key: string }[] = [
  { value: 'ENTRY_FRAGGER', label: 'Entry Fragger', key: 'roles.entryFragger' },
  { value: 'AWPER', label: 'AWPer', key: 'roles.awper' },
  { value: 'IGL', label: 'IGL', key: 'roles.igl' },
  { value: 'SUPPORT', label: 'Suporte', key: 'roles.support' },
  { value: 'LURKER', label: 'Lurker', key: 'roles.lurker' },
  { value: 'RIFLER', label: 'Rifler', key: 'roles.rifler' },
  { value: 'FLEX', label: 'Flex', key: 'roles.flex' },
];

export function getPlayerRoleLabel(
  role: PlayerRole | string | null | undefined,
  i18n?: { t: (key: string, params?: Record<string, string | number>) => string } | null,
): string | null {
  if (!role) return null;
  const upper = (role as string).toUpperCase();
  const found = PLAYER_ROLE_OPTIONS.find(
    (opt) => opt.value === upper || opt.label.toLowerCase() === (role as string).toLowerCase(),
  );
  if (i18n && found?.key) {
    return i18n.t(found.key);
  }
  return found ? found.label : role;
}

export const PLAYSTYLE_OPTIONS: { value: Playstyle; label: string; key: string }[] = [
  { value: 'CASUAL', label: 'Casual', key: 'playstyles.casual' },
  { value: 'COMPETITIVE', label: 'Competitivo', key: 'playstyles.competitive' },
  { value: 'TRYHARD', label: 'Tryhard', key: 'playstyles.tryhard' },
  { value: 'TEAM_ORIENTED', label: 'Focado em time', key: 'playstyles.teamOriented' },
  { value: 'SOLO_QUEUE', label: 'Solo queue', key: 'playstyles.soloQueue' },
];

export interface Cs2MapOption {
  value: Cs2Map;
  label: string;
  imageUrl?: string;
}

export const CS2_MAP_IMAGES: Record<Cs2Map, string | undefined> = {
  ANCIENT: '/assets/maps/cs2ancient.webp',
  ANUBIS: '/assets/maps/anubis.png',
  ALPINE: '/assets/maps/alpine.webp',
  CACHE: '/assets/maps/cache.jpg',
  DUST2: '/assets/maps/dust2.webp',
  INFERNO: '/assets/maps/inferno.webp',
  ITALY: '/assets/maps/italy.webp',
  MIRAGE: '/assets/maps/mirage.webp',
  NUKE: '/assets/maps/nuke.webp',
  OFFICE: '/assets/maps/office.webp',
  OVERPASS: '/assets/maps/overpass.webp',
  STRONGHOLD: '/assets/maps/stronghold.webp',
  TRAIN: '/assets/maps/train.webp',
  VERTIGO: '/assets/maps/vertigo.webp',
  WARDEN: '/assets/maps/warden.webp',
};

export const CS2_MAP_OPTIONS: Cs2MapOption[] = [
  { value: 'ANCIENT', label: 'Ancient', imageUrl: '/assets/maps/cs2ancient.webp' },
  { value: 'ANUBIS', label: 'Anubis', imageUrl: '/assets/maps/anubis.png' },
  { value: 'ALPINE', label: 'Alpine', imageUrl: '/assets/maps/alpine.webp' },
  { value: 'CACHE', label: 'Cache', imageUrl: '/assets/maps/cache.jpg' },
  { value: 'DUST2', label: 'Dust II', imageUrl: '/assets/maps/dust2.webp' },
  { value: 'INFERNO', label: 'Inferno', imageUrl: '/assets/maps/inferno.webp' },
  { value: 'ITALY', label: 'Italy', imageUrl: '/assets/maps/italy.webp' },
  { value: 'MIRAGE', label: 'Mirage', imageUrl: '/assets/maps/mirage.webp' },
  { value: 'NUKE', label: 'Nuke', imageUrl: '/assets/maps/nuke.webp' },
  { value: 'OFFICE', label: 'Office', imageUrl: '/assets/maps/office.webp' },
  { value: 'OVERPASS', label: 'Overpass', imageUrl: '/assets/maps/overpass.webp' },
  { value: 'STRONGHOLD', label: 'Stronghold', imageUrl: '/assets/maps/stronghold.webp' },
  { value: 'TRAIN', label: 'Train', imageUrl: '/assets/maps/train.webp' },
  { value: 'VERTIGO', label: 'Vertigo', imageUrl: '/assets/maps/vertigo.webp' },
  { value: 'WARDEN', label: 'Warden', imageUrl: '/assets/maps/warden.webp' },
];

