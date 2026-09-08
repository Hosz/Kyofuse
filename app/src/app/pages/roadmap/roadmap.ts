import { Component, inject, signal } from '@angular/core';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { DiscoverySidebarComponent } from '../../components/discovery/discovery-sidebar/discovery-sidebar';
import { ToastService } from '../../core/services/ui/toast.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { I18nService } from '../../core/i18n/i18n.service';

export interface RoadmapItem {
  id: string;
  icon: string;
  categoryKey: string;
  categoryGroup: 'MATCHMAKING_TIMES' | 'SOCIAL_FEED' | 'TATICAS_GUIAS';
  titleKey: string;
  descKey: string;
  status: 'PLANEJADO';
  estimatedQuarter: string;
}

@Component({
  selector: 'app-roadmap',
  imports: [AppSidebarComponent, DiscoverySidebarComponent, TranslatePipe],
  templateUrl: './roadmap.html',
  styleUrl: './roadmap.css',
})
export class RoadmapComponent {
  private toastService = inject(ToastService);
  private i18n = inject(I18nService);

  readonly activeFilter = signal<'ALL' | 'MATCHMAKING_TIMES' | 'SOCIAL_FEED' | 'TATICAS_GUIAS'>('ALL');

  readonly roadmapItems: RoadmapItem[] = [
    {
      id: 'duo-finder',
      icon: 'person_search',
      categoryKey: 'roadmap.catMatchmaking',
      categoryGroup: 'MATCHMAKING_TIMES',
      titleKey: 'roadmap.duoFinderTitle',
      descKey: 'roadmap.duoFinderDesc',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q4 2026',
    },
    {
      id: 'scrims-lfg',
      icon: 'sports_esports',
      categoryKey: 'roadmap.catTeamsCompetitive',
      categoryGroup: 'MATCHMAKING_TIMES',
      titleKey: 'roadmap.scrimsLfgTitle',
      descKey: 'roadmap.scrimsLfgDesc',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q4 2026',
    },
    {
      id: 'team-recruitment',
      icon: 'how_to_reg',
      categoryKey: 'roadmap.catRecruitment',
      categoryGroup: 'MATCHMAKING_TIMES',
      titleKey: 'roadmap.teamRecruitmentTitle',
      descKey: 'roadmap.teamRecruitmentDesc',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q1 2027',
    },
    {
      id: 'tournaments-hub',
      icon: 'emoji_events',
      categoryKey: 'roadmap.catTournaments',
      categoryGroup: 'MATCHMAKING_TIMES',
      titleKey: 'roadmap.tournamentsHubTitle',
      descKey: 'roadmap.tournamentsHubDesc',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q3 2027',
    },
    {
      id: 'tactics-hub',
      icon: 'menu_book',
      categoryKey: 'roadmap.catTacticsGuides',
      categoryGroup: 'TATICAS_GUIAS',
      titleKey: 'roadmap.tacticsHubTitle',
      descKey: 'roadmap.tacticsHubDesc',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q1 2027',
    },
    {
      id: 'reposts',
      icon: 'repeat',
      categoryKey: 'roadmap.catFeedSocial',
      categoryGroup: 'SOCIAL_FEED',
      titleKey: 'roadmap.repostsTitle',
      descKey: 'roadmap.repostsDesc',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q1 2027',
    },
    {
      id: 'badges',
      icon: 'military_tech',
      categoryKey: 'roadmap.catGamerProfile',
      categoryGroup: 'SOCIAL_FEED',
      titleKey: 'roadmap.badgesTitle',
      descKey: 'roadmap.badgesDesc',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q1 2027',
    },
    {
      id: 'polls',
      icon: 'poll',
      categoryKey: 'roadmap.catEngagement',
      categoryGroup: 'SOCIAL_FEED',
      titleKey: 'roadmap.pollsTitle',
      descKey: 'roadmap.pollsDesc',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q2 2027',
    },
    {
      id: 'media-uploads',
      icon: 'image',
      categoryKey: 'roadmap.catMedia',
      categoryGroup: 'SOCIAL_FEED',
      titleKey: 'roadmap.mediaUploadsTitle',
      descKey: 'roadmap.mediaUploadsDesc',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q2 2027',
    },
  ];

  setFilter(filter: 'ALL' | 'MATCHMAKING_TIMES' | 'SOCIAL_FEED' | 'TATICAS_GUIAS'): void {
    this.activeFilter.set(filter);
  }

  filteredItems(): RoadmapItem[] {
    const filter = this.activeFilter();
    if (filter === 'ALL') return this.roadmapItems;
    return this.roadmapItems.filter((item) => item.categoryGroup === filter);
  }

  notifyMe(item: RoadmapItem): void {
    const title = this.i18n.t(item.titleKey as any);
    const msg = this.i18n.t('roadmap.notifySuccess').replace('{title}', title);
    this.toastService.success(msg);
  }
}
