export type ReactionType = 'LIKE' | 'FIRE' | 'CLUTCH' | 'NICE_SHOT' | 'LOL';

export interface ReactionOption {
  type: ReactionType;
  icon: string;
  label: string;
}

/** Espelha o enum ReactionType do backend (reactions/enums/ReactionType.java). */
export const REACTION_OPTIONS: ReactionOption[] = [
  { type: 'LIKE', icon: 'favorite', label: 'Curtir' },
  { type: 'FIRE', icon: 'local_fire_department', label: 'Insano' },
  { type: 'CLUTCH', icon: 'bolt', label: 'Clutch' },
  { type: 'NICE_SHOT', icon: 'target', label: 'Mira boa' },
  { type: 'LOL', icon: 'sentiment_very_satisfied', label: 'Haha' },
];
