import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { CreateCommunityModalComponent } from '../../components/communities/create-community-modal/create-community-modal';
import { CommunityService } from '../../core/services/communities/community.service';
import { CommunityResponse } from '../../models/communities/community.model';

type HubTab = 'mine' | 'discover';

const SEARCH_DEBOUNCE_MS = 300;

@Component({
  selector: 'app-communities-hub',
  imports: [RouterLink, AppSidebarComponent, CreateCommunityModalComponent],
  templateUrl: './communities-hub.html',
  styleUrl: './communities-hub.css',
})
export class CommunitiesHubComponent implements OnDestroy {
  private communityService = inject(CommunityService);
  private router = inject(Router);
  private searchDebounce?: ReturnType<typeof setTimeout>;

  activeTab = signal<HubTab>('discover');
  createModalOpen = signal(false);

  loading = signal(true);
  error = signal<string | null>(null);
  communities = signal<CommunityResponse[]>([]);
  page = signal(0);
  totalPages = signal(0);

  myCommunitiesLoading = signal(true);
  myCommunitiesError = signal<string | null>(null);
  myCommunities = signal<CommunityResponse[]>([]);
  myCommunitiesPage = signal(0);
  myCommunitiesTotalPages = signal(0);
  private myCommunitiesLoaded = false;

  searchQuery = signal('');
  myCommunitiesQuery = signal('');

  /**
   * A listagem de "minhas comunidades" não tem filtro por nome no backend (ela deriva
   * de community_members, não de uma busca), então o filtro é aplicado sobre a página
   * já carregada — o que basta para a quantidade de comunidades que um usuário tem.
   */
  visibleMyCommunities = computed(() => {
    const query = this.myCommunitiesQuery().trim().toLowerCase();
    if (!query) return this.myCommunities();
    return this.myCommunities().filter((community) => community.communityName.toLowerCase().includes(query));
  });

  ngOnInit(): void {
    this.loadCommunities(0);
  }

  ngOnDestroy(): void {
    if (this.searchDebounce) clearTimeout(this.searchDebounce);
  }

  onSearchInput(value: string): void {
    this.searchQuery.set(value);
    if (this.searchDebounce) clearTimeout(this.searchDebounce);
    this.searchDebounce = setTimeout(() => this.loadCommunities(0), SEARCH_DEBOUNCE_MS);
  }

  onMyCommunitiesSearchInput(value: string): void {
    this.myCommunitiesQuery.set(value);
  }

  openCreateModal(): void {
    this.createModalOpen.set(true);
  }

  closeCreateModal(): void {
    this.createModalOpen.set(false);
  }

  onCommunityCreated(community: CommunityResponse): void {
    this.createModalOpen.set(false);
    this.router.navigate(['/comunidade', community.id]);
  }

  setTab(tab: HubTab): void {
    this.activeTab.set(tab);
    if (tab === 'mine') this.loadMyCommunitiesIfNeeded();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) return;
    this.loadCommunities(page);
  }

  goToMyCommunitiesPage(page: number): void {
    if (page < 0 || page >= this.myCommunitiesTotalPages()) return;
    this.loadMyCommunities(page);
  }

  visibilityLabel(visibility: string): string {
    return visibility === 'PRIVATE' ? 'Privada' : 'Pública';
  }

  private loadMyCommunitiesIfNeeded(): void {
    if (this.myCommunitiesLoaded) return;
    this.myCommunitiesLoaded = true;
    this.loadMyCommunities(0);
  }

  private loadMyCommunities(page: number): void {
    this.myCommunitiesLoading.set(true);
    this.myCommunitiesError.set(null);

    this.communityService.listMyCommunities(page).subscribe({
      next: (response) => {
        this.myCommunities.set(response.content);
        this.myCommunitiesPage.set(page);
        this.myCommunitiesTotalPages.set(response.totalPages);
        this.myCommunitiesLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch my communities:', error);
        this.myCommunitiesError.set('Não foi possível carregar suas comunidades.');
        this.myCommunitiesLoading.set(false);
      },
    });
  }

  private loadCommunities(page: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.communityService.listCommunities(this.searchQuery(), page).subscribe({
      next: (response) => {
        this.communities.set(response.content);
        this.page.set(page);
        this.totalPages.set(response.totalPages);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch communities:', error);
        this.error.set('Não foi possível carregar as comunidades.');
        this.loading.set(false);
      },
    });
  }
}
