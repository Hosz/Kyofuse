export type TeamStatus = 'ACTIVE' | 'RECRUITING' | 'CLOSED' | 'INACTIVE';
export type TeamMemberType = 'UNASSIGNED' | 'PLAYER' | 'SUBSTITUTE' | 'COACH' | 'MANAGER' | 'ANALYST';
export type TeamMemberStatus = 'ACTIVE' | 'LEFT' | 'REMOVED' | 'KICKED';

/** Espelham os enums do backend (teams/enums/*.java). */
export const TEAM_STATUS_OPTIONS: { value: TeamStatus; label: string }[] = [
  { value: 'ACTIVE', label: 'Ativo' },
  { value: 'RECRUITING', label: 'Recrutando' },
  { value: 'CLOSED', label: 'Fechado' },
  { value: 'INACTIVE', label: 'Inativo' },
];

export const TEAM_MEMBER_TYPE_OPTIONS: { value: TeamMemberType; label: string }[] = [
  { value: 'UNASSIGNED', label: 'Sem função' },
  { value: 'PLAYER', label: 'Jogador' },
  { value: 'SUBSTITUTE', label: 'Reserva' },
  { value: 'COACH', label: 'Coach' },
  { value: 'MANAGER', label: 'Manager' },
  { value: 'ANALYST', label: 'Analista' },
];

export const TEAM_MEMBER_STATUS_LABEL: Record<TeamMemberStatus, string> = {
  ACTIVE: 'Ativo',
  LEFT: 'Saiu',
  REMOVED: 'Removido',
  KICKED: 'Expulso',
};
