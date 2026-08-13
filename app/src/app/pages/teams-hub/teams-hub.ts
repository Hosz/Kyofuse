import { Component, OnDestroy, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { TeamService } from '../../core/services/teams/team.service';
import { TeamResponse } from '../../models/teams/team.model';
import { TEAM_STATUS_OPTIONS } from '../../shared/models/team-options.model';

type HubTab = 'mine' | 'discover';

const SEARCH_DEBOUNCE_MS = 300;

@Component({
  selector: 'app-teams-hub',
  imports: [RouterLink, AppSidebarComponent],
  templateUrl: './teams-hub.html',
  styleUrl: './teams-hub.css',
})
export class TeamsHubComponent implements OnDestroy {
  private teamService = inject(TeamService);
  private searchDebounce?: ReturnType<typeof setTimeout>;

  activeTab = signal<HubTab>('discover');

  loading = signal(true);
  error = signal<string | null>(null);
  teams = signal<TeamResponse[]>([]);
  searchQuery = signal('');

  myTeamsLoading = signal(true);
  myTeamsError = signal<string | null>(null);
  myTeams = signal<TeamResponse[]>([]);
  private myTeamsLoaded = false;

  ngOnInit(): void {
    this.loadTeams();
  }

  ngOnDestroy(): void {
    if (this.searchDebounce) clearTimeout(this.searchDebounce);
  }

  setTab(tab: HubTab): void {
    this.activeTab.set(tab);
    if (tab === 'mine') this.loadMyTeamsIfNeeded();
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
