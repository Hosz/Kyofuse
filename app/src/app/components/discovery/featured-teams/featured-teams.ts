import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TeamService } from '../../../core/services/teams/team.service';
import { TeamResponse } from '../../../models/teams/team.model';
import { TEAM_STATUS_OPTIONS, TeamStatus } from '../../../shared/models/team-options.model';

export interface DisplayFeaturedTeam {
  id?: string;
  slug?: string;
  name: string;
  avatarUrl?: string | null;
  statusText: string;
  region?: string | null;
}

@Component({
  selector: 'app-featured-teams',
  imports: [RouterLink],
  templateUrl: './featured-teams.html',
  styleUrl: './featured-teams.css',
})
export class FeaturedTeamsComponent implements OnInit {
  private teamService = inject(TeamService);

  customTeams = input<TeamResponse[] | null>(null);

  teams = signal<DisplayFeaturedTeam[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const custom = this.customTeams();
    if (custom && custom.length > 0) {
      this.teams.set(custom.map((t) => this.mapTeamToDisplay(t)));
      this.loading.set(false);
      return;
    }
    this.loadFeaturedTeams();
  }

  statusLabel(status: string): string {
    return TEAM_STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status;
  }

  private mapTeamToDisplay(team: TeamResponse): DisplayFeaturedTeam {
    return {
      id: team.id,
      slug: team.slug,
      name: team.name,
      avatarUrl: team.avatarUrl,
      statusText: this.statusLabel(team.status),
      region: team.region,
    };
  }

  private loadFeaturedTeams(): void {
    this.loading.set(true);
    this.error.set(null);

    this.teamService.listingTeams({ status: 'ACTIVE' }, 0, 3).subscribe({
      next: (response) => {
        this.teams.set(response.content.map((t) => this.mapTeamToDisplay(t)));
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load featured teams', err);
        this.error.set('Não foi possível carregar os times em destaque.');
        this.loading.set(false);
      },
    });
  }
}