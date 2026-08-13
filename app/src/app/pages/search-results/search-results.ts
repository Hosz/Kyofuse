import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { FeedTabsComponent } from '../../components/feed/feed-tabs/feed-tabs';
import { FeedTab } from '../../shared/models/social.model';
import { TeamService } from '../../core/services/teams/team.service';
import { TeamResponse } from '../../models/teams/team.model';
import { TEAM_STATUS_OPTIONS } from '../../shared/models/team-options.model';

type ResultsTab = 'teams' | 'profiles';

@Component({
  selector: 'app-search-results',
  imports: [RouterLink, AppSidebarComponent, FeedTabsComponent],
  templateUrl: './search-results.html',
  styleUrl: './search-results.css',
})
export class SearchResultsComponent {
  private route = inject(ActivatedRoute);
  private teamService = inject(TeamService);

  query = signal('');
  activeTab = signal<ResultsTab>('teams');

  teams = signal<TeamResponse[]>([]);
  teamsTotal = signal(0);
  teamsPage = signal(0);
  teamsTotalPages = signal(0);
  teamsLoading = signal(true);

  tabs = computed<FeedTab[]>(() => [
    { label: 'Times', active: this.activeTab() === 'teams' },
    { label: 'Perfis', active: this.activeTab() === 'profiles' },
  ]);

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const q = params.get('q') ?? '';
      this.query.set(q);
      this.teamsPage.set(0);
      this.loadTeams();
    });
  }

  onTabSelected(tab: FeedTab): void {
    this.activeTab.set(tab.label === 'Times' ? 'teams' : 'profiles');
  }

  statusLabel(status: string): string {
    return TEAM_STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status;
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.teamsTotalPages()) return;
    this.teamsPage.set(page);
    this.loadTeams();
  }

  private loadTeams(): void {
    const query = this.query().trim();
    if (!query) {
      this.teams.set([]);
      this.teamsTotal.set(0);
      this.teamsTotalPages.set(0);
      this.teamsLoading.set(false);
      return;
    }

    this.teamsLoading.set(true);
    this.teamService.listingTeams({ name: query }, this.teamsPage()).subscribe({
      next: (response) => {
        this.teams.set(response.content);
        this.teamsTotal.set(response.totalElements);
        this.teamsTotalPages.set(response.totalPages);
        this.teamsLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to search teams:', error);
        this.teamsLoading.set(false);
      },
    });
  }
}
