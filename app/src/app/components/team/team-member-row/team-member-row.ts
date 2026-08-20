import { Component, computed, input, output } from '@angular/core';
import { TeamMemberResponse } from '../../../models/teams/team-member.model';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';

/**
 * Linha de membro do time. Clicar abre o cartão do usuário; o dono vê as ações de
 * gestão nos três pontinhos.
 */
@Component({
  selector: 'app-team-member-row',
  imports: [],
  templateUrl: './team-member-row.html',
  styleUrl: './team-member-row.css',
})
export class TeamMemberRowComponent {
  member = input.required<TeamMemberResponse>();
  /** Nome de usuário do dono do time — a linha dele ganha o selo e perde as ações. */
  ownerName = input<string | null>(null);
  /** Ligado só para quem pode gerir o elenco. */
  canManage = input(false);
  roleLabel = input<string>('');
  statusLabel = input<string>('');

  selected = output<TeamMemberResponse>();

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  isOwner = computed(() => this.member().userName === this.ownerName());

  displayName = computed(() => this.member().nickname || this.member().userName);
}
