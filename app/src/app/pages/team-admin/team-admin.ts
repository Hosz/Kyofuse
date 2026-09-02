import { Component, computed, HostListener, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ModalComponent } from '../../components/shared/modal/modal';
import { TeamService } from '../../core/services/teams/team.service';
import { TeamMemberService } from '../../core/services/teams/team-member.service';
import { TeamInviteService } from '../../core/services/teams/team-invite.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { MediaService } from '../../core/services/media/media.service';
import { ToastService } from '../../core/services/ui/toast.service';
import { TeamResponse, UpdateTeamRequest } from '../../models/teams/team.model';
import { TeamMemberEditRequest, TeamMemberResponse } from '../../models/teams/team-member.model';
import { PLAYER_ROLE_OPTIONS, PlayerRole } from '../../shared/models/profile-options.model';
import {
  TEAM_MEMBER_STATUS_LABEL,
  TEAM_MEMBER_TYPE_OPTIONS,
  TEAM_STATUS_OPTIONS,
  TeamMemberType,
  TeamStatus,
} from '../../shared/models/team-options.model';
import { ConfirmDialogComponent } from '../../components/shared/confirm-dialog/confirm-dialog';
import { gamerProfileResponse } from '../../models/profile/gamer-profile.model';
import { FALLBACK_AVATAR_URL, TEAM_FALLBACK_AVATAR_URL } from '../../shared/utils/format.util';
import { TeamMemberRowComponent } from '../../components/team/team-member-row/team-member-row';
import { TeamMemberModalComponent } from '../../components/team/team-member-modal/team-member-modal';
import { getCountryFlagUrl, getCountryOptions } from '../../shared/models/location-options.model';

type SectionId = 'geral' | 'requisitos' | 'papeis' | 'membros' | 'recrutamento';

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

import { RoleIconComponent } from '../../components/shared/role-icon/role-icon';

@Component({
  selector: 'app-team-admin',
  imports: [RouterLink, AppSidebarComponent, ModalComponent, ConfirmDialogComponent, TeamMemberRowComponent, TeamMemberModalComponent, RoleIconComponent],
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
  private profileService = inject(ProfileService);
  private mediaService = inject(MediaService);
  private toastService = inject(ToastService);
  private observer?: IntersectionObserver;
  private inviteSearchDebounce?: ReturnType<typeof setTimeout>;

  readonly roleOptions = PLAYER_ROLE_OPTIONS;
  readonly memberTypeOptions = TEAM_MEMBER_TYPE_OPTIONS;
  readonly statusOptions = TEAM_STATUS_OPTIONS;
  readonly memberStatusLabel = TEAM_MEMBER_STATUS_LABEL;

  readonly sections: { id: SectionId; label: string; icon: string }[] = [
    { id: 'geral', label: 'Informações Gerais', icon: 'info' },
    { id: 'requisitos', label: 'Requisitos Competitivos', icon: 'military_tech' },
    { id: 'papeis', label: 'Papéis Necessários', icon: 'groups' },
    { id: 'membros', label: 'Membros', icon: 'group' },
    { id: 'recrutamento', label: 'Recrutamento', icon: 'person_search' },
  ];

  activeSection = signal<SectionId>('geral');

  loading = signal(true);
  notFound = signal(false);
  saving = signal(false);
  uploadingAvatar = signal(false);
  uploadingBanner = signal(false);
  error = signal<string | null>(null);

  onAvatarFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadingAvatar.set(true);
    this.mediaService.uploadImage(file).subscribe({
      next: (res) => {
        this.avatarUrl.set(res.url);
        this.uploadingAvatar.set(false);
        this.toastService.info('Escudo carregado. Clique em Salvar para aplicar.');
      },
      error: (err) => {
        console.error('Failed to upload team avatar:', err);
        this.toastService.error('Erro ao enviar o escudo do time.');
        this.uploadingAvatar.set(false);
      },
    });
  }

  removeAvatar(): void {
    this.avatarUrl.set('');
  }

  onBannerFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadingBanner.set(true);
    this.mediaService.uploadImage(file).subscribe({
      next: (res) => {
        this.bannerUrl.set(res.url);
        this.uploadingBanner.set(false);
        this.toastService.info('Banner carregado. Clique em Salvar para aplicar.');
      },
      error: (err) => {
        console.error('Failed to upload team banner:', err);
        this.toastService.error('Erro ao enviar o banner do time.');
        this.uploadingBanner.set(false);
      },
    });
  }

  removeBanner(): void {
    this.bannerUrl.set('');
  }

  name = signal('');
  description = signal('');
  avatarUrl = signal('');
  bannerUrl = signal('');
  region = signal('');
  readonly countryOptions = getCountryOptions();
  readonly flagUrl = computed(() => getCountryFlagUrl(this.region()));
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

  inviteUsername = signal('');
  inviteMessage = signal('');
  inviteMemberType = signal<TeamMemberType | ''>('');
  inviteRoleInTeam = signal<PlayerRole | ''>('');
  inviting = signal(false);
  inviteError = signal<string | null>(null);
  inviteSuccess = signal(false);

  suggestedProfiles = signal<gamerProfileResponse[]>([]);
  suggestionsLoading = signal(false);
  suggestionsOpen = signal(false);

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const el = event.target as HTMLElement | null;
    if (!el?.closest('#invite-autocomplete-container')) {
      this.suggestionsOpen.set(false);
    }
  }

  onInviteUsernameInput(value: string): void {
    this.inviteUsername.set(value);
    this.inviteError.set(null);
    this.inviteSuccess.set(false);

    const query = value.trim().replace(/^@/, '');
    if (!query) {
      if (this.inviteSearchDebounce) clearTimeout(this.inviteSearchDebounce);
      this.suggestedProfiles.set([]);
      this.suggestionsOpen.set(false);
      this.suggestionsLoading.set(false);
      return;
    }

    this.suggestionsOpen.set(true);
    this.suggestionsLoading.set(true);

    if (this.inviteSearchDebounce) clearTimeout(this.inviteSearchDebounce);
    this.inviteSearchDebounce = setTimeout(() => {
      this.profileService.listingProfiles({ username: query }, 0, 5).subscribe({
        next: (response) => {
          this.suggestedProfiles.set(response.content);
          this.suggestionsLoading.set(false);
        },
        error: (err) => {
          console.error('Failed to fetch profile suggestions:', err);
          this.suggestedProfiles.set([]);
          this.suggestionsLoading.set(false);
        },
      });
    }, 250);
  }

  onInviteUsernameFocus(): void {
    const query = this.inviteUsername().trim().replace(/^@/, '');
    if (query) {
      this.suggestionsOpen.set(true);
      if (this.suggestedProfiles().length === 0 && !this.suggestionsLoading()) {
        this.onInviteUsernameInput(this.inviteUsername());
      }
    }
  }

  selectSuggestedProfile(profile: gamerProfileResponse): void {
    this.inviteUsername.set(profile.username);
    this.suggestionsOpen.set(false);
    this.suggestedProfiles.set([]);
  }

  closeSuggestions(): void {
    this.suggestionsOpen.set(false);
  }

  removeConfirmMember = signal<TeamMemberResponse | null>(null);
  removing = signal(false);
  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  readonly teamFallbackAvatar = TEAM_FALLBACK_AVATAR_URL;

  /** Jogadores anunciando que procuram time — candidatos para convidar. */
  lookingForTeam = signal<gamerProfileResponse[]>([]);
  lookingLoading = signal(false);
  lookingLoadingMore = signal(false);
  private lookingPage = signal(0);
  private lookingLastPage = signal(true);

  hasMoreLookingToLoad = computed(() => !this.lookingLastPage());

  loadLookingForTeam(page: number = 0): void {
    const id = this.teamId();
    if (!id) return;

    if (page === 0) this.lookingLoading.set(true);
    else this.lookingLoadingMore.set(true);

    this.teamService.listPlayersLookingForTeam(id, page).subscribe({
      next: (response) => {
        this.lookingForTeam.update((list) => (page === 0 ? response.content : [...list, ...response.content]));
        this.lookingPage.set(page);
        this.lookingLastPage.set(response.last);
        this.lookingLoading.set(false);
        this.lookingLoadingMore.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch players looking for a team:', error);
        this.lookingLoading.set(false);
        this.lookingLoadingMore.set(false);
      },
    });
  }

  loadMoreLooking(): void {
    if (this.lookingLoadingMore() || this.lookingLastPage()) return;
    this.loadLookingForTeam(this.lookingPage() + 1);
  }

  /** Reaproveita o convite por username no formulário acima. */
  invitePlayer(profile: gamerProfileResponse): void {
    this.inviteUsername.set(profile.username);
    this.inviteMember();
  }

  /** Nome do dono, guardado ao carregar o time: a linha dele leva o selo e não tem ações. */
  ownerName = signal<string | null>(null);
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
        this.ownerName.set(team.ownerName);
        this.seedFromTeam(team);
        this.loading.set(false);
        this.setupSectionObserver();
        this.loadMembers(id);
        this.loadLookingForTeam();
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
    if (this.inviteSearchDebounce) clearTimeout(this.inviteSearchDebounce);
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
      avatarUrl: this.avatarUrl().trim(),
      bannerUrl: this.bannerUrl().trim(),
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
    const username = this.inviteUsername().trim().replace(/^@/, '');
    if (!teamId || !username || this.inviting()) return;

    this.inviting.set(true);
    this.inviteError.set(null);
    this.inviteSuccess.set(false);
    this.suggestionsOpen.set(false);

    this.teamInviteService
      .inviteUser(teamId, username, {
        message: this.inviteMessage().trim() || undefined,
        proposedMemberType: this.inviteMemberType() || undefined,
        proposedRoleInTeam: this.inviteRoleInTeam() || undefined,
      })
      .subscribe({
        next: () => {
          this.inviting.set(false);
          this.inviteSuccess.set(true);
          this.inviteUsername.set('');
          this.suggestedProfiles.set([]);
          this.inviteMessage.set('');
          this.inviteMemberType.set('');
          this.inviteRoleInTeam.set('');
        },
        error: (error) => {
          console.error('Failed to invite member:', error);
          this.inviting.set(false);
          this.inviteError.set(error?.error?.message ?? 'Não foi possível enviar o convite. Confira o usuário informado.');
        },
      });
  }

  selectedMember = signal<TeamMemberResponse | null>(null);

  openMember(member: TeamMemberResponse): void {
    this.removeError.set(null);
    this.selectedMember.set(member);
  }

  closeMember(): void {
    if (this.removing()) return;
    this.selectedMember.set(null);
  }

  savingMember = signal(false);

  /** Preencher um papel anunciado tira a vaga do anúncio no backend — por isso o time
   * é relido, para a seção "Papéis Necessários" refletir a mudança na hora. */
  editMember(request: TeamMemberEditRequest): void {
    const id = this.teamId();
    const member = this.selectedMember();
    if (!id || !member || this.savingMember()) return;

    this.savingMember.set(true);
    this.removeError.set(null);

    this.teamMemberService.editMember(id, member.userId, request).subscribe({
      next: (updated) => {
        this.savingMember.set(false);
        this.selectedMember.set(null);
        this.members.update((list) => list.map((m) => (m.userId === member.userId ? { ...m, ...updated } : m)));
        this.reloadRequiredRoles(id);
      },
      error: (error) => {
        console.error('Failed to edit member:', error);
        this.savingMember.set(false);
        this.removeError.set(error?.error?.message ?? 'Não foi possível salvar as alterações.');
      },
    });
  }

  private reloadRequiredRoles(teamId: string): void {
    this.teamService.detailTeam(teamId).subscribe({
      next: (team) => this.requiredRoles.set(team.requiredRoles ?? []),
      error: (error) => console.error('Failed to refresh required roles:', error),
    });
  }

  openRemoveConfirm(member: TeamMemberResponse): void {
    this.selectedMember.set(null);
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

  leaveConfirmOpen = signal(false);
  leaving = signal(false);
  leaveError = signal<string | null>(null);

  askLeaveTeam(): void {
    this.leaveError.set(null);
    this.leaveConfirmOpen.set(true);
  }

  cancelLeaveTeam(): void {
    if (this.leaving()) return;
    this.leaveConfirmOpen.set(false);
  }

  leaveTeam(): void {
    const teamId = this.teamId();
    if (!teamId || this.leaving()) return;

    this.leaving.set(true);
    this.leaveError.set(null);

    this.teamMemberService.leaveTeam(teamId).subscribe({
      next: () => {
        this.leaving.set(false);
        this.leaveConfirmOpen.set(false);
        this.router.navigateByUrl('/times');
      },
      error: (error) => {
        console.error('Failed to leave team:', error);
        this.leaving.set(false);
        this.leaveError.set('Não foi possível sair do time. Tente novamente.');
      },
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
    this.avatarUrl.set(team.avatarUrl ?? '');
    this.bannerUrl.set(team.bannerUrl ?? '');
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
