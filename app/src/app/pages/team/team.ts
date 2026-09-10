import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { FeedTabsComponent } from '../../components/feed/feed-tabs/feed-tabs';
import { FeedTab } from '../../shared/models/social.model';
import { TeamService } from '../../core/services/teams/team.service';
import { TeamMemberService } from '../../core/services/teams/team-member.service';
import { CommunityService } from '../../core/services/communities/community.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { CommunityResponse } from '../../models/communities/community.model';
import { TeamResponse } from '../../models/teams/team.model';
import { TeamMemberEditRequest, TeamMemberResponse } from '../../models/teams/team-member.model';
import { PLAYER_ROLE_OPTIONS, getPlayerRoleLabel } from '../../shared/models/profile-options.model';
import { TEAM_MEMBER_STATUS_LABEL, TEAM_STATUS_OPTIONS } from '../../shared/models/team-options.model';
import { ConfirmDialogComponent } from '../../components/shared/confirm-dialog/confirm-dialog';
import { ModalComponent } from '../../components/shared/modal/modal';
import { TeamMemberRowComponent } from '../../components/team/team-member-row/team-member-row';
import { TeamMemberModalComponent } from '../../components/team/team-member-modal/team-member-modal';
import { getCountryFlagUrl } from '../../shared/models/location-options.model';
import { ToastService } from '../../core/services/ui/toast.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

type ViewMode = 'visitor' | 'member' | 'admin';
type InfoTab = 'description' | 'requisites' | 'members' | 'history';

@Component({
  selector: 'app-team',
  imports: [RouterLink, AppSidebarComponent, FeedTabsComponent, ConfirmDialogComponent, ModalComponent, TeamMemberRowComponent, TeamMemberModalComponent, TranslatePipe],
  templateUrl: './team.html',
  styleUrl: './team.css',
})
export class TeamComponent {
  /** Vinculado automaticamente ao parâmetro de rota :teamId (withComponentInputBinding). */
  teamId = input<string | null>(null);

  private teamService = inject(TeamService);
  private teamMemberService = inject(TeamMemberService);
  private communityService = inject(CommunityService);
  private profileService = inject(ProfileService);
  private toastService = inject(ToastService);
  readonly i18n = inject(I18nService);

  onApplyToTeam(): void {
    this.toastService.info('O envio de candidaturas e solicitações de entrada em times estará disponível em breve!');
  }

  readonly roleOptions = PLAYER_ROLE_OPTIONS;
  readonly memberStatusLabel = TEAM_MEMBER_STATUS_LABEL;

  loading = signal(true);
  notFound = signal(false);
  team = signal<TeamResponse | null>(null);
  readonly flagUrl = computed(() => getCountryFlagUrl(this.team()?.region));

  members = signal<TeamMemberResponse[]>([]);
  membersLoading = signal(true);
  private membersPage = signal(0);
  private membersLastPage = signal(true);
  membersLoadingMore = signal(false);
  hasMoreMembersToLoad = computed(() => !this.membersLastPage());

  viewMode = signal<ViewMode>('visitor');
  activeInfoTab = signal<InfoTab>('description');
  myUsername = signal<string | null>(null);

  leaving = signal(false);
  leaveError = signal<string | null>(null);

  /** Todo Team nasce com uma Community vinculada, mas um time antigo pode não ter —
   * por isso o 404 aqui é um resultado esperado, não um erro. */
  community = signal<CommunityResponse | null>(null);

  infoTabs = computed<FeedTab[]>(() => [
    { id: 'description', label: 'Descrição', active: this.activeInfoTab() === 'description' },
    { id: 'requisites', label: 'Requisitos', active: this.activeInfoTab() === 'requisites' },
    { id: 'members', label: 'Membros', active: this.activeInfoTab() === 'members' },
    { id: 'history', label: 'Histórico de Atividade', active: this.activeInfoTab() === 'history' },
  ]);

  ngOnInit(): void {
    const id = this.teamId();
    if (!id) {
      this.notFound.set(true);
      this.loading.set(false);
      return;
    }

    this.teamService.detailTeam(id).subscribe({
      next: (team) => {
        this.team.set(team);
        this.loading.set(false);
        this.resolveViewMode();
        this.loadMembers(team.id);
        this.loadCommunity(team.id);
      },
      error: (error) => {
        console.error('Failed to fetch team:', error);
        this.notFound.set(true);
        this.loading.set(false);
      },
    });
  }

  onInfoTabSelected(tab: FeedTab): void {
    if (tab.id === 'description') this.activeInfoTab.set('description');
    else if (tab.id === 'requisites') this.activeInfoTab.set('requisites');
    else if (tab.id === 'members') this.activeInfoTab.set('members');
    else this.activeInfoTab.set('history');
  }

  statusLabel(status: string): string {
    return TEAM_STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status;
  }

  roleLabel(role: string): string {
    return getPlayerRoleLabel(role, this.i18n) ?? role;
  }

  selectedMember = signal<TeamMemberResponse | null>(null);
  removingMember = signal(false);
  memberActionError = signal<string | null>(null);

  isOwner = computed(() => {
    const team = this.team();
    const myUser = this.myUsername();
    return !!team && !!myUser && team.ownerName === myUser;
  });

  isManager = computed(() => {
    const myUser = this.myUsername();
    if (!myUser) return false;
    return this.members().some((m) => m.userName === myUser && m.memberType === 'MANAGER');
  });

  isHead = computed(() => this.isOwner() || this.isManager());

  /** Só o dono gere o elenco — o backend valida checkUserIsOwner nessas ações. */
  canManageMembers = computed(() => this.isOwner());

  createCommunityConfirmOpen = signal(false);
  creatingCommunity = signal(false);
  createCommunityError = signal<string | null>(null);

  attachCommunityModalOpen = signal(false);
  availableCommunities = signal<CommunityResponse[]>([]);
  loadingAvailableCommunities = signal(false);
  selectedCommunityId = signal<string | null>(null);
  attachingCommunity = signal(false);
  attachCommunityError = signal<string | null>(null);

  openCreateCommunityConfirm(): void {
    this.createCommunityError.set(null);
    this.createCommunityConfirmOpen.set(true);
  }

  cancelCreateCommunity(): void {
    if (this.creatingCommunity()) return;
    this.createCommunityConfirmOpen.set(false);
  }

  confirmCreateCommunity(): void {
    const id = this.team()?.id ?? this.teamId();
    if (!id || this.creatingCommunity()) return;

    this.creatingCommunity.set(true);
    this.createCommunityError.set(null);

    this.teamService.createCommunityFromTeam(id).subscribe({
      next: (comm) => {
        this.community.set(comm);
        this.creatingCommunity.set(false);
        this.createCommunityConfirmOpen.set(false);
        this.toastService.success(this.i18n.t('teams.communityCreatedSuccess'));
      },
      error: (err) => {
        this.creatingCommunity.set(false);
        this.createCommunityError.set(err?.error?.message ?? 'Erro ao criar comunidade.');
      },
    });
  }

  openAttachCommunityModal(): void {
    const id = this.team()?.id ?? this.teamId();
    if (!id) return;

    this.attachCommunityModalOpen.set(true);
    this.attachCommunityError.set(null);
    this.selectedCommunityId.set(null);
    this.loadingAvailableCommunities.set(true);

    this.teamService.listAvailableCommunities(id).subscribe({
      next: (list) => {
        this.availableCommunities.set(list);
        this.loadingAvailableCommunities.set(false);
      },
      error: (err) => {
        console.error('Failed to list available communities:', err);
        this.loadingAvailableCommunities.set(false);
      },
    });
  }

  closeAttachCommunityModal(): void {
    if (this.attachingCommunity()) return;
    this.attachCommunityModalOpen.set(false);
  }

  confirmAttachCommunity(): void {
    const teamId = this.team()?.id ?? this.teamId();
    const commId = this.selectedCommunityId();
    if (!teamId || !commId || this.attachingCommunity()) return;

    this.attachingCommunity.set(true);
    this.attachCommunityError.set(null);

    this.teamService.attachCommunity(teamId, commId).subscribe({
      next: (comm) => {
        this.community.set(comm);
        this.attachingCommunity.set(false);
        this.attachCommunityModalOpen.set(false);
        this.toastService.success(this.i18n.t('teams.communityAttachedSuccess'));
      },
      error: (err) => {
        this.attachingCommunity.set(false);
        this.attachCommunityError.set(err?.error?.message ?? 'Erro ao vincular comunidade.');
      },
    });
  }

  openMember(member: TeamMemberResponse): void {
    this.memberActionError.set(null);
    this.selectedMember.set(member);
  }

  closeMember(): void {
    if (this.removingMember()) return;
    this.selectedMember.set(null);
  }

  savingMember = signal(false);

  /** Depois de salvar, os papéis necessários são recarregados: preencher um papel
   * anunciado tira a vaga do anúncio no backend. */
  editMember(request: TeamMemberEditRequest): void {
    const id = this.team()?.id ?? this.teamId();
    const member = this.selectedMember();
    if (!id || !member || this.savingMember()) return;

    this.savingMember.set(true);
    this.memberActionError.set(null);

    this.teamMemberService.editMember(id, member.userId, request).subscribe({
      next: (updated) => {
        this.savingMember.set(false);
        this.selectedMember.set(null);
        this.members.update((list) => list.map((m) => (m.userId === member.userId ? { ...m, ...updated } : m)));
        this.reloadTeam(id);
      },
      error: (error) => {
        console.error('Failed to edit member:', error);
        this.savingMember.set(false);
        this.memberActionError.set(error?.error?.message ?? 'Não foi possível salvar as alterações.');
      },
    });
  }

  private reloadTeam(teamId: string): void {
    this.teamService.detailTeam(teamId).subscribe({
      next: (team) => this.team.set(team),
      error: (error) => console.error('Failed to refresh team:', error),
    });
  }

  removeMember(member: TeamMemberResponse): void {
    const id = this.team()?.id ?? this.teamId();
    if (!id || this.removingMember()) return;

    this.removingMember.set(true);
    this.memberActionError.set(null);

    this.teamMemberService.removeMember(id, member.userId).subscribe({
      next: () => {
        this.removingMember.set(false);
        this.selectedMember.set(null);
        this.members.update((list) => list.filter((m) => m.userId !== member.userId));
      },
      error: (error) => {
        console.error('Failed to remove member:', error);
        this.removingMember.set(false);
        this.memberActionError.set(error?.error?.message ?? 'Não foi possível remover esse membro.');
      },
    });
  }

  leaveConfirmOpen = signal(false);

  askLeaveTeam(): void {
    this.leaveError.set(null);
    this.leaveConfirmOpen.set(true);
  }

  cancelLeaveTeam(): void {
    if (this.leaving()) return;
    this.leaveConfirmOpen.set(false);
  }

  leaveTeam(): void {
    const id = this.team()?.id ?? this.teamId();
    if (!id || this.leaving()) return;

    this.leaveConfirmOpen.set(false);
    this.leaving.set(true);
    this.leaveError.set(null);

    this.teamMemberService.leaveTeam(id).subscribe({
      next: () => {
        this.leaving.set(false);
        this.viewMode.set('visitor');
        this.loadMembers(id);
      },
      error: (error) => {
        console.error('Failed to leave team:', error);
        this.leaving.set(false);
        this.leaveError.set('Não foi possível sair do time. Tente novamente.');
      },
    });
  }

  loadMoreMembers(): void {
    const teamId = this.team()?.id ?? this.teamId();
    if (!teamId || this.membersLoadingMore() || this.membersLastPage()) return;
    this.membersLoadingMore.set(true);
    this.loadMembers(teamId, this.membersPage() + 1);
  }

  private loadCommunity(teamId: string): void {
    this.communityService.detailCommunityByTeam(teamId).subscribe({
      next: (community) => this.community.set(community),
      error: () => this.community.set(null),
    });
  }

  private loadMembers(teamId: string, page: number = 0): void {
    if (page === 0) this.membersLoading.set(true);
    this.teamMemberService.listMembers(teamId, page).subscribe({
      next: (response) => {
        /** A aba "Membros" deve mostrar só quem está ativo hoje. */
        const active = response.content.filter((member) => member.status === 'ACTIVE');
        this.members.update((list) => (page === 0 ? active : [...list, ...active]));
        this.membersLastPage.set(response.last);
        this.membersPage.set(page);
        this.membersLoading.set(false);
        this.membersLoadingMore.set(false);
        if (page === 0) this.resolveViewMode();
      },
      error: (error) => {
        console.error('Failed to fetch team members:', error);
        this.membersLoading.set(false);
        this.membersLoadingMore.set(false);
        this.resolveViewMode();
      },
    });
  }

  private resolveViewMode(): void {
    const team = this.team();
    if (!team) return;

    const username = this.myUsername();
    if (username) {
      this.applyViewMode(team, username);
      return;
    }

    this.profileService.myProfile().subscribe({
      next: (profile) => {
        this.myUsername.set(profile.username);
        this.applyViewMode(team, profile.username);
      },
      error: (error) => console.error('Failed to fetch my profile:', error),
    });
  }

  private applyViewMode(team: TeamResponse, username: string): void {
    const myMember = this.members().find((m) => m.userName === username);
    if (username === team.ownerName || (myMember && myMember.memberType === 'MANAGER')) {
      this.viewMode.set('admin');
    } else if (myMember) {
      this.viewMode.set('member');
    } else {
      this.viewMode.set('visitor');
    }
  }
}
