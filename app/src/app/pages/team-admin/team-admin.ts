import { Component, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ModalComponent } from '../../components/shared/modal/modal';
import { TeamService } from '../../core/services/teams/team.service';
import { TeamMemberService } from '../../core/services/teams/team-member.service';
import { TeamInviteService } from '../../core/services/teams/team-invite.service';
import { TeamResponse, UpdateTeamRequest } from '../../models/teams/team.model';
import { TeamMemberResponse } from '../../models/teams/team-member.model';
import { PLAYER_ROLE_OPTIONS, PlayerRole } from '../../shared/models/profile-options.model';
import {
  TEAM_MEMBER_STATUS_LABEL,
  TEAM_MEMBER_TYPE_OPTIONS,
  TEAM_STATUS_OPTIONS,
  TeamMemberType,
  TeamStatus,
} from '../../shared/models/team-options.model';

type SectionId = 'geral' | 'requisitos' | 'papeis' | 'membros';

/** Só pra dar variedade visual às cartas de papel (mesma ideia dos mapas na edição de perfil). */
const ROLE_ACCENT_HUE: Record<PlayerRole, number> = {
  ENTRY_FRAGGER: 10,
  AWPER: 200,
  IGL: 260,
  SUPPORT: 130,
  LURKER: 20,
  RIFLER: 30,
  FLEX: 190,
};

@Component({
  selector: 'app-team-admin',
  imports: [RouterLink, AppSidebarComponent, ModalComponent],
  templateUrl: './team-admin.html',
  styleUrl: './team-admin.css',
})
export class TeamAdminComponent {
  /** Vinculado automaticamente ao parâmetro de rota :teamId (withComponentInputBinding). */
  teamId = input<string | null>(null);

  private router = inject(Router);
  private teamService = inject(TeamService);
  private teamMemberService = inject(TeamMemberService);
  private teamInviteService = inject(TeamInviteService);
  private observer?: IntersectionObserver;

  readonly roleOptions = PLAYER_ROLE_OPTIONS;
  readonly memberTypeOptions = TEAM_MEMBER_TYPE_OPTIONS;
  readonly statusOptions = TEAM_STATUS_OPTIONS;
  readonly memberStatusLabel = TEAM_MEMBER_STATUS_LABEL;

  readonly sections: { id: SectionId; label: string; icon: string }[] = [
    { id: 'geral', label: 'Informações Gerais', icon: 'info' },
    { id: 'requisitos', label: 'Requisitos Competitivos', icon: 'military_tech' },
    { id: 'papeis', label: 'Papéis Necessários', icon: 'groups' },
    { id: 'membros', label: 'Membros', icon: 'group' },
  ];

  activeSection = signal<SectionId>('geral');

  loading = signal(true);
  notFound = signal(false);
  saving = signal(false);
  error = signal<string | null>(null);

  name = signal('');
  description = signal('');
  region = signal('');
  minPremierRating = signal<number | null>(null);
  maxPremierRating = signal<number | null>(null);
  minFaceitLevel = signal<number | null>(null);
  maxFaceitLevel = signal<number | null>(null);
  minGcRank = signal<number | null>(null);
  maxGcRank = signal<number | null>(null);
  status = signal<TeamStatus | ''>('');
  requiredRoles = signal<PlayerRole[]>([]);

  members = signal<TeamMemberResponse[]>([]);
  membersLoading = signal(false);
  private membersPage = signal(0);
  private membersLastPage = signal(true);
  membersLoadingMore = signal(false);
  hasMoreMembersToLoad = computed(() => !this.membersLastPage());

  inviteUserId = signal('');
  inviteMessage = signal('');
  inviteMemberType = signal<TeamMemberType | ''>('');
  inviteRoleInTeam = signal<PlayerRole | ''>('');
  inviting = signal(false);
  inviteError = signal<string | null>(null);
  inviteSuccess = signal(false);

  removeConfirmMember = signal<TeamMemberResponse | null>(null);
  removing = signal(false);
  removeError = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.teamId();
    if (!id) {
      this.notFound.set(true);
      this.loading.set(false);
      this.setupSectionObserver();
      return;
    }

    this.teamService.detailTeam(id).subscribe({
      next: (team) => {
        this.seedFromTeam(team);
        this.loading.set(false);
        this.setupSectionObserver();
        this.loadMembers(id);
      },
      error: (error) => {
        console.error('Failed to fetch team:', error);
        this.notFound.set(true);
        this.error.set('Não foi possível carregar o time.');
        this.loading.set(false);
        this.setupSectionObserver();
      },
    });
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private setupSectionObserver(): void {
    setTimeout(() => {
      this.observer = new IntersectionObserver(
        (entries) => {
          for (const entry of entries) {
            if (entry.isIntersecting) {
              this.activeSection.set(entry.target.id as SectionId);
            }
          }
        },
        { rootMargin: '-15% 0px -70% 0px', threshold: 0 },
      );

      for (const section of this.sections) {
        const el = document.getElementById(section.id);
        if (el) this.observer.observe(el);
      }
    });
  }

  scrollToSection(id: SectionId): void {
    this.activeSection.set(id);
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  roleAccent(role: PlayerRole): string {
    return `hsl(${ROLE_ACCENT_HUE[role]} 32% 14%)`;
  }

  toggleRequiredRole(role: PlayerRole): void {
    this.requiredRoles.update((roles) =>
      roles.includes(role) ? roles.filter((r) => r !== role) : [...roles, role],
    );
  }

  submit(): void {
    const teamId = this.teamId();
    if (!teamId || this.saving()) return;
    if (!this.name().trim()) {
      this.error.set('Informe o nome do time.');
      this.scrollToSection('geral');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const request: UpdateTeamRequest = {
      name: this.name().trim(),
      description: this.description().trim(),
      region: this.region().trim(),
      minPremierRating: this.minPremierRating() ?? undefined,
      maxPremierRating: this.maxPremierRating() ?? undefined,
      minFaceitLevel: this.minFaceitLevel() ?? undefined,
      maxFaceitLevel: this.maxFaceitLevel() ?? undefined,
      minGcRank: this.minGcRank() ?? undefined,
      maxGcRank: this.maxGcRank() ?? undefined,
      status: this.status() || undefined,
    };

    this.teamService.editTeam(teamId, request).subscribe({
      next: () => {
        this.teamService.manageRequiredRoles(teamId, { requiredRoles: this.requiredRoles() }).subscribe({
          next: () => {
            this.saving.set(false);
            this.router.navigateByUrl(`/times/${teamId}`);
          },
          error: (error) => {
            console.error('Failed to update required roles:', error);
            this.saving.set(false);
            this.error.set('O time foi salvo, mas não foi possível atualizar os papéis necessários.');
          },
        });
      },
      error: (error) => {
        console.error('Failed to edit team:', error);
        this.saving.set(false);
        this.error.set('Não foi possível salvar as alterações do time.');
      },
    });
  }

  inviteMember(): void {
    const teamId = this.teamId();
    const userId = this.inviteUserId().trim();
    if (!teamId || !userId || this.inviting()) return;

    this.inviting.set(true);
    this.inviteError.set(null);
    this.inviteSuccess.set(false);

    this.teamInviteService
      .inviteUser(teamId, userId, {
        message: this.inviteMessage().trim() || undefined,
        proposedMemberType: this.inviteMemberType() || undefined,
        proposedRoleInTeam: this.inviteRoleInTeam() || undefined,
      })
      .subscribe({
        next: () => {
          this.inviting.set(false);
          this.inviteSuccess.set(true);
          this.inviteUserId.set('');
          this.inviteMessage.set('');
          this.inviteMemberType.set('');
          this.inviteRoleInTeam.set('');
        },
        error: (error) => {
          console.error('Failed to invite member:', error);
          this.inviting.set(false);
          this.inviteError.set('Não foi possível enviar o convite. Confira o ID informado.');
        },
      });
  }

  openRemoveConfirm(member: TeamMemberResponse): void {
    this.removeError.set(null);
    this.removeConfirmMember.set(member);
  }

  closeRemoveConfirm(): void {
    if (this.removing()) return;
    this.removeConfirmMember.set(null);
  }

  confirmRemoveMember(): void {
    const teamId = this.teamId();
    const member = this.removeConfirmMember();
    if (!teamId || !member || this.removing()) return;

    this.removing.set(true);
    this.removeError.set(null);

    this.teamMemberService.removeMember(teamId, member.userId).subscribe({
      next: () => {
        this.removing.set(false);
        this.removeConfirmMember.set(null);
        this.loadMembers(teamId);
      },
      error: (error) => {
        console.error('Failed to remove member:', error);
        this.removing.set(false);
        this.removeError.set('Não foi possível remover esse membro. Tente novamente.');
      },
    });
  }

  leaveTeam(): void {
    const teamId = this.teamId();
    if (!teamId) return;

    this.teamMemberService.leaveTeam(teamId).subscribe({
      next: () => this.router.navigateByUrl('/times'),
      error: (error) => console.error('Failed to leave team:', error),
    });
  }

  loadMoreMembers(): void {
    const teamId = this.teamId();
    if (!teamId || this.membersLoadingMore() || this.membersLastPage()) return;
    this.membersLoadingMore.set(true);
    this.loadMembers(teamId, this.membersPage() + 1);
  }

  private loadMembers(teamId: string, page: number = 0): void {
    if (page === 0) this.membersLoading.set(true);
    this.teamMemberService.listMembers(teamId, page).subscribe({
      next: (response) => {
        this.members.update((list) => (page === 0 ? response.content : [...list, ...response.content]));
        this.membersLastPage.set(response.last);
        this.membersPage.set(page);
        this.membersLoading.set(false);
        this.membersLoadingMore.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch team members:', error);
        this.membersLoading.set(false);
        this.membersLoadingMore.set(false);
      },
    });
  }

  private seedFromTeam(team: TeamResponse): void {
    this.name.set(team.name);
    this.description.set(team.description ?? '');
    this.region.set(team.region ?? '');
    this.minPremierRating.set(team.minPremierRating ?? null);
    this.maxPremierRating.set(team.maxPremierRating ?? null);
    this.minFaceitLevel.set(team.minFaceitLevel ?? null);
    this.maxFaceitLevel.set(team.maxFaceitLevel ?? null);
    this.minGcRank.set(team.minGcRank ?? null);
    this.maxGcRank.set(team.maxGcRank ?? null);
    this.status.set(team.status ?? '');
    this.requiredRoles.set(team.requiredRoles ?? []);
  }
}
