import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { CreateTeamModalComponent } from '../../components/teams/create-team-modal/create-team-modal';
import { TeamService } from '../../core/services/teams/team.service';
import { TeamResponse } from '../../models/teams/team.model';
import { TEAM_STATUS_OPTIONS } from '../../shared/models/team-options.model';

type HubTab = 'mine' | 'discover';

const SEARCH_DEBOUNCE_MS = 300;

@Component({
  selector: 'app-teams-hub',
  imports: [RouterLink, AppSidebarComponent, CreateTeamModalComponent],
  templateUrl: './teams-hub.html',
  styleUrl: './teams-hub.css',
})
export class TeamsHubComponent implements OnDestroy {
  private teamService = inject(TeamService);
  private router = inject(Router);
  private searchDebounce?: ReturnType<typeof setTimeout>;

  createModalOpen = signal(false);

  activeTab = signal<HubTab>('discover');

  loading = signal(true);
  error = signal<string | null>(null);
  teams = signal<TeamResponse[]>([]);
  searchQuery = signal('');

  myTeamsLoading = signal(true);
  myTeamsError = signal<string | null>(null);
  myTeams = signal<TeamResponse[]>([]);
  private myTeamsLoaded = false;

  myTeamsQuery = signal('');

  /**
   * "Meus times" não tem filtro por nome no backend (deriva de team_members), então a
   * busca é aplicada sobre a página já carregada — suficiente para a quantidade de
   * times que um usuário costuma ter.
   */
  visibleMyTeams = computed(() => {
    const query = this.myTeamsQuery().trim().toLowerCase();
    if (!query) return this.myTeams();
    return this.myTeams().filter((team) => team.name.toLowerCase().includes(query));
  });

  ngOnInit(): void {
    this.loadTeams();
  }

  ngOnDestroy(): void {
    if (this.searchDebounce) clearTimeout(this.searchDebounce);
  }

  openCreateModal(): void {
    this.createModalOpen.set(true);
  }

  closeCreateModal(): void {
    this.createModalOpen.set(false);
  }

  onTeamCreated(team: TeamResponse): void {
    this.createModalOpen.set(false);
    this.router.navigate(['/times', team.id]);
  }

  setTab(tab: HubTab): void {
    this.activeTab.set(tab);
    if (tab === 'mine') this.loadMyTeamsIfNeeded();
  }

  onMyTeamsSearchInput(value: string): void {
    this.myTeamsQuery.set(value);
  }

  onSearchInput(value: string): void {
    this.searchQuery.set(value);
    if (this.searchDebounce) clearTimeout(this.searchDebounce);
    this.searchDebounce = setTimeout(() => this.loadTeams(), SEARCH_DEBOUNCE_MS);
  }

  statusLabel(status: string): string {
    return TEAM_STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status;
  }

  private loadMyTeamsIfNeeded(): void {
    if (this.myTeamsLoaded) return;
    this.myTeamsLoaded = true;
    this.myTeamsLoading.set(true);
    this.myTeamsError.set(null);

    this.teamService.listingMyTeams().subscribe({
      next: (response) => {
        this.myTeams.set(response.content);
        this.myTeamsLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch my teams:', error);
        this.myTeamsError.set('Não foi possível carregar seus times.');
        this.myTeamsLoading.set(false);
      },
    });
  }

  private loadTeams(): void {
    this.loading.set(true);
    this.error.set(null);

    const name = this.searchQuery().trim();
    this.teamService.listingTeams(name ? { name } : {}).subscribe({
      next: (response) => {
        this.teams.set(response.content);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch teams:', error);
        this.error.set('Não foi possível carregar os times.');
        this.loading.set(false);
      },
    });
  }
}
