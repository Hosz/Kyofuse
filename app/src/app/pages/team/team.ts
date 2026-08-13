import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { FeedTabsComponent } from '../../components/feed/feed-tabs/feed-tabs';
import { FeedTab } from '../../shared/models/social.model';
import { TeamService } from '../../core/services/teams/team.service';
import { TeamMemberService } from '../../core/services/teams/team-member.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { TeamResponse } from '../../models/teams/team.model';
import { TeamMemberResponse } from '../../models/teams/team-member.model';
import { PLAYER_ROLE_OPTIONS } from '../../shared/models/profile-options.model';
import { TEAM_MEMBER_STATUS_LABEL, TEAM_STATUS_OPTIONS } from '../../shared/models/team-options.model';

type ViewMode = 'visitor' | 'member' | 'admin';
type InfoTab = 'description' | 'requisites' | 'members' | 'history';

@Component({
  selector: 'app-team',
  imports: [RouterLink, AppSidebarComponent, FeedTabsComponent],
  templateUrl: './team.html',
  styleUrl: './team.css',
})
export class TeamComponent {
  /** Vinculado automaticamente ao parâmetro de rota :teamId (withComponentInputBinding). */
  teamId = input<string | null>(null);

  private teamService = inject(TeamService);
  private teamMemberService = inject(TeamMemberService);
  private profileService = inject(ProfileService);

  readonly roleOptions = PLAYER_ROLE_OPTIONS;
  readonly memberStatusLabel = TEAM_MEMBER_STATUS_LABEL;

  loading = signal(true);
  notFound = signal(false);
  team = signal<TeamResponse | null>(null);

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

  infoTabs = computed<FeedTab[]>(() => [
    { label: 'Descrição', active: this.activeInfoTab() === 'description' },
    { label: 'Requisitos', active: this.activeInfoTab() === 'requisites' },
    { label: 'Membros', active: this.activeInfoTab() === 'members' },
    { label: 'Histórico de Atividade', active: this.activeInfoTab() === 'history' },
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
        this.loadMembers(id);
      },
      error: (error) => {
        console.error('Failed to fetch team:', error);
        this.notFound.set(true);
        this.loading.set(false);
      },
    });
  }

  onInfoTabSelected(tab: FeedTab): void {
    if (tab.label === 'Descrição') this.activeInfoTab.set('description');
    else if (tab.label === 'Requisitos') this.activeInfoTab.set('requisites');
    else if (tab.label === 'Membros') this.activeInfoTab.set('members');
    else this.activeInfoTab.set('history');
  }

  statusLabel(status: string): string {
    return TEAM_STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status;
  }

  roleLabel(role: string): string {
    return this.roleOptions.find((option) => option.value === role)?.label ?? role;
  }

  leaveTeam(): void {
    const id = this.teamId();
    if (!id || this.leaving()) return;

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
    const teamId = this.teamId();
    if (!teamId || this.membersLoadingMore() || this.membersLastPage()) return;
    this.membersLoadingMore.set(true);
    this.loadMembers(teamId, this.membersPage() + 1);
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
      },
    });
  }

  private resolveViewMode(): void {
    const team = this.team();
    if (!team) return;

    this.profileService.myProfile().subscribe({
      next: (profile) => {
        this.myUsername.set(profile.username);
        if (profile.username === team.ownerName) {
          this.viewMode.set('admin');
        } else if (this.members().some((member) => member.userName === profile.username)) {
          this.viewMode.set('member');
        } else {
          this.viewMode.set('visitor');
        }
      },
      error: (error) => console.error('Failed to fetch my profile:', error),
    });
  }
}
