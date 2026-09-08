import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { DiscoverySidebarComponent } from '../../components/discovery/discovery-sidebar/discovery-sidebar';
import { LeaderboardService } from '../../core/services/leaderboard/leaderboard.service';
import { LeaderboardEntryResponse, UserRankResponse } from '../../models/leaderboard/leaderboard.model';
import { FALLBACK_AVATAR_URL } from '../../shared/utils/format.util';

import { TranslatePipe } from '../../core/i18n/translate.pipe';

@Component({
  selector: 'app-leaderboard',
  standalone: true,
  imports: [CommonModule, RouterLink, AppSidebarComponent, DiscoverySidebarComponent, TranslatePipe],
  templateUrl: './leaderboard.html',
  styleUrl: './leaderboard.css',
})
export class LeaderboardComponent implements OnInit {
  private readonly leaderboardService = inject(LeaderboardService);

  readonly topPlayers = signal<LeaderboardEntryResponse[]>([]);
  readonly myRank = signal<UserRankResponse | null>(null);
  readonly surroundingPlayers = signal<LeaderboardEntryResponse[]>([]);
  readonly loading = signal(true);
  readonly activeTab = signal<'top' | 'around'>('top');

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);

    this.leaderboardService.getTopPlayers(50).subscribe({
      next: (players) => {
        this.topPlayers.set(players);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao buscar top players:', err);
        this.loading.set(false);
      },
    });

    this.leaderboardService.getMyRank().subscribe({
      next: (rank) => this.myRank.set(rank),
      error: (err) => console.debug('Erro ao buscar rank do usuário:', err),
    });

    this.leaderboardService.getAroundMe(4).subscribe({
      next: (around) => this.surroundingPlayers.set(around),
      error: (err) => console.debug('Erro ao buscar competidores ao redor:', err),
    });
  }

  getRankBadgeClass(rank: number): string {
    if (rank === 1) return 'bg-amber-400/20 text-amber-300 border-amber-400/40 shadow-amber-400/10 shadow-lg';
    if (rank === 2) return 'bg-slate-300/20 text-slate-200 border-slate-300/40';
    if (rank === 3) return 'bg-amber-700/20 text-amber-600 border-amber-700/40';
    return 'bg-surface-container text-on-surface-variant border-outline-variant/30';
  }
}
