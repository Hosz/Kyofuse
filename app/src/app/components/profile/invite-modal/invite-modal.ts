import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { TeamService } from '../../../core/services/teams/team.service';
import { TeamInviteService } from '../../../core/services/teams/team-invite.service';
import { CurrentUserService } from '../../../core/services/profile/current-user.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { TeamResponse } from '../../../models/teams/team.model';

const TEAMS_PAGE_SIZE = 50;

type InviteState = 'idle' | 'sending' | 'sent' | 'failed';

/**
 * Convite de um usuário para os meus times.
 *
 * Só times que eu sou dono aparecem: o backend recusa o convite de qualquer outra
 * pessoa (teamChecker.checkUserIsOwner). Antes, o modal recebia listas que nunca eram
 * preenchidas e o botão "Convidar" só mudava de cor sem chamar a API.
 */
@Component({
  selector: 'app-invite-modal',
  imports: [ModalComponent],
  templateUrl: './invite-modal.html',
  styleUrl: './invite-modal.css',
})
export class InviteModalComponent {
  open = input(false);
  targetName = input<string>('');
  targetUsername = input.required<string>();

  closed = output<void>();

  private teamService = inject(TeamService);
  private teamInviteService = inject(TeamInviteService);
  private currentUser = inject(CurrentUserService);
  private toastService = inject(ToastService);

  private myTeams = signal<TeamResponse[]>([]);
  loading = signal(false);
  private loaded = false;

  private states = signal<Record<string, InviteState>>({});

  /** O backend só deixa o dono convidar, então filtrar aqui evita oferecer o que vai falhar. */
  ownedTeams = computed(() => {
    const myId = this.currentUser.userId();
    return myId ? this.myTeams().filter((team) => team.ownerId === myId) : [];
  });

  constructor() {
    effect(() => {
      if (this.open() && !this.loaded) {
        this.loaded = true;
        this.loadTeams();
      }
    });
  }

  stateOf(teamId: string): InviteState {
    return this.states()[teamId] ?? 'idle';
  }

  label(teamId: string): string {
    switch (this.stateOf(teamId)) {
      case 'sending':
        return 'Enviando...';
      case 'sent':
        return 'Convite enviado';
      case 'failed':
        return 'Tentar de novo';
      default:
        return 'Convidar';
    }
  }

  invite(team: TeamResponse): void {
    if (this.stateOf(team.id) === 'sending' || this.stateOf(team.id) === 'sent') return;
    this.setState(team.id, 'sending');

    this.teamInviteService.inviteUser(team.id, this.targetUsername(), {}).subscribe({
      next: () => {
        this.setState(team.id, 'sent');
        this.toastService.success(`Convite para ${team.name} enviado.`);
      },
      error: (error) => {
        console.error('Failed to invite user to team:', error);
        this.setState(team.id, 'failed');
        // O backend explica o motivo (já é membro, convite pendente, etc.).
        this.toastService.error(error?.error?.message ?? 'Não foi possível enviar o convite.');
      },
    });
  }

  private setState(teamId: string, state: InviteState): void {
    this.states.update((current) => ({ ...current, [teamId]: state }));
  }

  private loadTeams(): void {
    this.loading.set(true);

    this.teamService.listingMyTeams(0, TEAMS_PAGE_SIZE).subscribe({
      next: (response) => {
        this.myTeams.set(response.content);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch my teams:', error);
        this.loading.set(false);
        this.toastService.error('Não foi possível carregar seus times.');
      },
    });
  }
}
