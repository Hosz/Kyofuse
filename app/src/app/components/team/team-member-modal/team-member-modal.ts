import { Component, computed, effect, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ModalComponent } from '../../shared/modal/modal';
import { TeamMemberResponse } from '../../../models/teams/team-member.model';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { PLAYER_ROLE_OPTIONS, PlayerRole } from '../../../shared/models/profile-options.model';
import { TEAM_MEMBER_TYPE_OPTIONS, TeamMemberType } from '../../../shared/models/team-options.model';
import { TeamMemberEditRequest } from '../../../models/teams/team-member.model';

/**
 * Cartão do membro do time: leva ao perfil e, para quem pode gerir o elenco, reúne as
 * ações sobre aquela pessoa.
 *
 * Expulsar é o que o backend suporta hoje. Vote kick e banimento aparecem desabilitados
 * porque não existem endpoints para eles — melhor mostrar o caminho do que esconder.
 */
@Component({
  selector: 'app-team-member-modal',
  imports: [RouterLink, ModalComponent],
  templateUrl: './team-member-modal.html',
  styleUrl: './team-member-modal.css',
})
export class TeamMemberModalComponent {
  member = input<TeamMemberResponse | null>(null);
  canManage = input(false);
  isOwner = input(false);
  roleLabel = input<string>('');
  statusLabel = input<string>('');
  removing = input(false);
  saving = input(false);
  error = input<string | null>(null);

  closed = output<void>();
  removeRequested = output<TeamMemberResponse>();
  editRequested = output<TeamMemberEditRequest>();

  readonly roleOptions = PLAYER_ROLE_OPTIONS;
  readonly memberTypeOptions = TEAM_MEMBER_TYPE_OPTIONS;

  editing = signal(false);
  /** '' representa "sem papel": o backend só troca o que vier preenchido. */
  draftRole = signal<PlayerRole | ''>('');
  draftMemberType = signal<TeamMemberType | ''>('');

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  displayName = computed(() => this.member()?.nickname || this.member()?.userName || '');

  /** O backend recusa uma edição sem nenhum campo. */
  canSave = computed(() => this.draftRole() !== '' || this.draftMemberType() !== '');

  constructor() {
    // Sempre que outro membro é aberto, o formulário volta a refletir o que ele tem hoje.
    effect(() => {
      const member = this.member();
      this.editing.set(false);
      this.draftRole.set(member?.roleInTeam ?? '');
      this.draftMemberType.set(member?.memberType ?? '');
    });
  }

  startEditing(): void {
    this.editing.set(true);
  }

  cancelEditing(): void {
    if (this.saving()) return;
    const member = this.member();
    this.draftRole.set(member?.roleInTeam ?? '');
    this.draftMemberType.set(member?.memberType ?? '');
    this.editing.set(false);
  }

  save(): void {
    if (!this.canSave() || this.saving()) return;

    const role = this.draftRole();
    const memberType = this.draftMemberType();

    this.editRequested.emit({
      ...(role !== '' ? { roleInTeam: role } : {}),
      ...(memberType !== '' ? { memberType } : {}),
    });
  }

  /** O backend recusa remover o dono, então a ação nem aparece na linha dele. */
  showActions = computed(() => this.canManage() && !this.isOwner());
}
