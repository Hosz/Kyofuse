import { Component, OnInit, OnDestroy, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { FeedTabsComponent } from '../../components/feed/feed-tabs/feed-tabs';
import { FeedTab } from '../../shared/models/social.model';
import { TeamService } from '../../core/services/teams/team.service';
import { TeamResponse } from '../../models/teams/team.model';
import { ProfileService } from '../../core/services/profile/profile.service';
import { FollowService } from '../../core/services/follow/follow.service';
import { ToastService } from '../../core/services/ui/toast.service';
import { gamerProfileResponse } from '../../models/profile/gamer-profile.model';
import { TEAM_STATUS_OPTIONS } from '../../shared/models/team-options.model';
import { RoleIconComponent } from '../../components/shared/role-icon/role-icon';
import { getPlayerRoleLabel } from '../../shared/models/profile-options.model';
import { FALLBACK_AVATAR_URL } from '../../shared/utils/format.util';

import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { I18nService } from '../../core/i18n/i18n.service';

type ResultsTab = 'teams' | 'profiles';

const SEARCH_DEBOUNCE_MS = 300;
const PAGE_SIZE = 12;

@Component({
  selector: 'app-search-results',
  imports: [RouterLink, AppSidebarComponent, FeedTabsComponent, RoleIconComponent, TranslatePipe],
  templateUrl: './search-results.html',
  styleUrl: './search-results.css',
})
export class SearchResultsComponent implements OnInit, OnDestroy {
  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  roleLabel = (role: any) => getPlayerRoleLabel(role, this.i18n);

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private teamService = inject(TeamService);
  private profileService = inject(ProfileService);
  private followService = inject(FollowService);
  private toastService = inject(ToastService);
  readonly i18n = inject(I18nService);

  private searchDebounce?: ReturnType<typeof setTimeout>;

  query = signal('');
  searchInput = signal('');
  activeTab = signal<ResultsTab>('profiles');

  isExploreMode = computed(() => !this.query().trim());

  teams = signal<TeamResponse[]>([]);
  teamsTotal = signal(0);
  teamsPage = signal(0);
  teamsTotalPages = signal(0);
  teamsLoading = signal(true);

  profiles = signal<gamerProfileResponse[]>([]);
  profilesTotal = signal(0);
  profilesPage = signal(0);
  profilesTotalPages = signal(0);
  profilesLoading = signal(true);

  private readonly followedUserIds = signal(new Set<string>());
  private readonly processingFollowIds = signal(new Set<string>());

  tabs = computed<FeedTab[]>(() => {
    const isExplore = this.isExploreMode();
    return [
      { id: 'profiles', label: isExplore ? this.i18n.t('search.suggestedProfiles') : this.i18n.t('search.profilesTab'), active: this.activeTab() === 'profiles' },
      { id: 'teams', label: isExplore ? this.i18n.t('search.featuredTeams') : this.i18n.t('search.teamsTab'), active: this.activeTab() === 'teams' },
    ];
  });

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const q = params.get('q') ?? '';
      this.query.set(q);
      this.searchInput.set(q);

      const tabParam = params.get('tab');
      if (tabParam === 'teams' || tabParam === 'times') {
        this.activeTab.set('teams');
      } else if (tabParam === 'profiles' || tabParam === 'perfis') {
        this.activeTab.set('profiles');
      } else if (q) {
        this.activeTab.set('teams');
      } else {
        this.activeTab.set('profiles');
      }

      this.teamsPage.set(0);
      this.profilesPage.set(0);
      this.loadTeams();
      this.loadProfiles();
    });
  }

  ngOnDestroy(): void {
    if (this.searchDebounce) clearTimeout(this.searchDebounce);
  }

  onTabSelected(tab: FeedTab): void {
    const nextTab: ResultsTab = tab.id === 'teams' ? 'teams' : 'profiles';
    this.activeTab.set(nextTab);
    this.router.navigate([], {
      queryParams: { tab: nextTab },
      queryParamsHandling: 'merge',
    });
  }

  statusLabel(status: string): string {
    return TEAM_STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status;
  }

  onSearchChange(value: string): void {
    this.searchInput.set(value);
    if (this.searchDebounce) clearTimeout(this.searchDebounce);
    this.searchDebounce = setTimeout(() => {
      const trimmed = value.trim();
      this.router.navigate([], {
        queryParams: { q: trimmed || null, tab: this.activeTab() },
        queryParamsHandling: 'merge',
      });
    }, SEARCH_DEBOUNCE_MS);
  }

  clearSearch(): void {
    this.searchInput.set('');
    this.router.navigate([], {
      queryParams: { q: null, tab: this.activeTab() },
      queryParamsHandling: 'merge',
    });
  }

  isFollowing(userId: string): boolean {
    return this.followedUserIds().has(userId);
  }

  isProcessing(userId: string): boolean {
    return this.processingFollowIds().has(userId);
  }

  onToggleFollow(profile: gamerProfileResponse): void {
    const userId = profile.userId;
    if (!userId || this.isProcessing(userId)) return;

    const alreadyFollowing = this.isFollowing(userId);

    this.followedUserIds.update((set) => {
      const next = new Set(set);
      if (alreadyFollowing) {
        next.delete(userId);
      } else {
        next.add(userId);
      }
      return next;
    });

    this.processingFollowIds.update((set) => new Set(set).add(userId));

    if (alreadyFollowing) {
      this.followService.unfollowUser(userId).subscribe({
        next: () => {
          this.processingFollowIds.update((set) => {
            const next = new Set(set);
            next.delete(userId);
            return next;
          });
        },
        error: (err) => {
          console.error('Failed to unfollow', err);
          this.followedUserIds.update((set) => new Set(set).add(userId));
          this.processingFollowIds.update((set) => {
            const next = new Set(set);
            next.delete(userId);
            return next;
          });
          this.toastService.error('Não foi possível deixar de seguir o usuário.');
        },
      });
    } else {
      this.followService.followUser(userId).subscribe({
        next: () => {
          this.processingFollowIds.update((set) => {
            const next = new Set(set);
            next.delete(userId);
            return next;
          });
          this.toastService.success(`Você agora segue @${profile.username}`);
        },
        error: (err) => {
          console.error('Failed to follow', err);
          this.followedUserIds.update((set) => {
            const next = new Set(set);
            next.delete(userId);
            return next;
          });
          this.processingFollowIds.update((set) => {
            const next = new Set(set);
            next.delete(userId);
            return next;
          });
          this.toastService.error('Não foi possível seguir o usuário.');
        },
      });
    }
  }

  goToTeamsPage(page: number): void {
    if (page < 0 || page >= this.teamsTotalPages()) return;
    this.teamsPage.set(page);
    this.loadTeams();
  }

  goToProfilesPage(page: number): void {
    if (page < 0 || page >= this.profilesTotalPages()) return;
    this.profilesPage.set(page);
    this.loadProfiles();
  }

  private loadTeams(): void {
    this.teamsLoading.set(true);
    const query = this.query().trim();

    if (query) {
      this.teamService.listingTeams({ name: query }, this.teamsPage(), PAGE_SIZE).subscribe({
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
    } else {
      this.teamService.listingTeams({ status: 'ACTIVE' }, this.teamsPage(), PAGE_SIZE).subscribe({
        next: (response) => {
          this.teams.set(response.content);
          this.teamsTotal.set(response.totalElements);
          this.teamsTotalPages.set(response.totalPages);
          this.teamsLoading.set(false);
        },
        error: (error) => {
          console.error('Failed to load featured teams:', error);
          this.teamsLoading.set(false);
        },
      });
    }
  }

  private loadProfiles(): void {
    this.profilesLoading.set(true);
    const query = this.query().trim();

    if (query) {
      this.profileService.listingProfiles({ username: query }, this.profilesPage(), PAGE_SIZE).subscribe({
        next: (response) => {
          this.profiles.set(response.content);
          this.profilesTotal.set(response.totalElements);
          this.profilesTotalPages.set(response.totalPages);
          this.profilesLoading.set(false);
        },
        error: (error) => {
          console.error('Failed to search profiles:', error);
          this.profilesLoading.set(false);
        },
      });
    } else {
      this.followService.getSuggestions(this.profilesPage(), PAGE_SIZE).subscribe({
        next: (response) => {
          this.profiles.set(response.content);
          this.profilesTotal.set(response.totalElements);
          this.profilesTotalPages.set(response.totalPages);
          this.profilesLoading.set(false);
        },
        error: (error) => {
          console.error('Failed to load suggested profiles:', error);
          this.profilesLoading.set(false);
        },
      });
    }
  }
}
