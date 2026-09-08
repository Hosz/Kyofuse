import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Post } from '../../shared/models/social.model';
import { DiscoverySidebarComponent } from '../../components/discovery/discovery-sidebar/discovery-sidebar';
import { PostComposerComponent } from '../../components/feed/post-composer/post-composer';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { FeedListComponent } from '../../components/feed/feed-list/feed-list';
import { PinCommunityModalComponent } from '../../components/feed/pin-community-modal/pin-community-modal';
import { DragScrollDirective } from '../../shared/directives/drag-scroll.directive';
import { InfiniteScrollDirective } from '../../shared/directives/infinite-scroll.directive';
import { SkeletonComponent } from '../../components/shared/skeleton/skeleton';
import { PostsService } from '../../core/services/posts/posts.service';
import { CommunityService } from '../../core/services/communities/community.service';
import { ToastService } from '../../core/services/ui/toast.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { CommunityResponse } from '../../models/communities/community.model';
import { toPost } from '../../shared/utils/mappers.util';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { I18nService } from '../../core/i18n/i18n.service';

/**
 * Abas fixas do topo. As demais abas são ids de comunidade fixada — como são UUIDs,
 * nunca colidem com esses dois valores.
 */
const FOR_YOU = 'for-you';
const FOLLOWING = 'following';

type ActiveFeed = typeof FOR_YOU | typeof FOLLOWING | string;

@Component({
  selector: 'app-home',
  imports: [
    AppSidebarComponent,
    PostComposerComponent,
    FeedListComponent,
    DiscoverySidebarComponent,
    PinCommunityModalComponent,
    DragScrollDirective,
    InfiniteScrollDirective,
    SkeletonComponent,
    RouterLink,
    TranslatePipe,
  ],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent {

  postsService = inject(PostsService);
  userService = inject(ProfileService);
  private communityService = inject(CommunityService);
  private toastService = inject(ToastService);
  private i18n = inject(I18nService);

  posts = signal<Post[]>([]);
  loading = signal(true);

  private page = signal(0);
  private lastPage = signal(true);
  loadingMore = signal(false);
  hasMoreToLoad = computed(() => !this.lastPage());

  /** Comunidades fixadas viram abas ao lado das fixas, como no X. */
  pinnedCommunities = signal<CommunityResponse[]>([]);
  activeFeed = signal<ActiveFeed>(FOR_YOU);
  pinModalOpen = signal(false);

  readonly forYou = FOR_YOU;
  readonly following = FOLLOWING;

  /**
   * Até uma comunidade fixada, as abas dividem a largura entre si e nada rola: com duas
   * ou três, esticar fica mais harmônico do que deixar um vazio à direita. Da segunda
   * comunidade em diante elas voltam à largura natural e a faixa passa a rolar.
   */
  stretchTabs = computed(() => this.pinnedCommunities().length <= 1);

  /** "Para Você" e "Seguindo" não têm comunidade por trás — só elas mostram o compositor. */
  isFixedFeed = computed(() => this.activeFeed() === FOR_YOU || this.activeFeed() === FOLLOWING);

  activeCommunity = computed(() =>
    this.pinnedCommunities().find((community) => community.id === this.activeFeed()) ?? null,
  );

  emptyTitle = computed(() => {
    return this.i18n.t('feed.emptyTitle');
  });

  emptyMessage = computed(() => {
    return this.i18n.t('feed.emptyMessage');
  });

  emptyIcon = computed(() => {
    if (this.activeFeed() === FOLLOWING) {
      return 'person_search';
    }
    if (this.activeFeed() !== FOR_YOU) {
      return 'groups';
    }
    return 'dynamic_feed';
  });

  ngOnInit() {
    this.loadFeed(0);
    this.loadPinnedCommunities();
  }

  selectFeed(feed: ActiveFeed): void {
    if (this.activeFeed() === feed) return;
    this.activeFeed.set(feed);
    this.posts.set([]);
    this.loading.set(true);
    this.loadFeed(0);
  }

  openPinModal(): void {
    this.pinModalOpen.set(true);
  }

  closePinModal(): void {
    this.pinModalOpen.set(false);
  }

  onPinnedChanged(pinned: CommunityResponse[]): void {
    this.pinnedCommunities.set(pinned);
    // Se a comunidade aberta deixou de ser fixada, o feed volta pro geral em vez de
    // ficar preso numa aba que não existe mais.
    if (!this.isFixedFeed() && !pinned.some((community) => community.id === this.activeFeed())) {
      this.selectFeed(FOR_YOU);
    }
  }

  loadMore(): void {
    if (this.loadingMore() || this.lastPage()) return;
    this.loadingMore.set(true);
    this.loadFeed(this.page() + 1);
  }

  onAuthorBlocked(authorId: string): void {
    this.posts.update((list) => list.filter((post) => post.author.id !== authorId));
  }

  onPostDeleted(postId: string): void {
    this.posts.update((list) => list.filter((post) => post.id !== postId));
  }

  private loadPinnedCommunities(): void {
    this.communityService.listPinnedCommunities().subscribe({
      next: (communities) => this.pinnedCommunities.set(communities),
      error: (error) => console.error('Failed to fetch pinned communities:', error),
    });
  }

  private loadFeed(page: number): void {
    const feed = this.activeFeed();
    const request$ =
      feed === FOR_YOU
        ? this.postsService.getFeed(page)
        : feed === FOLLOWING
          ? this.postsService.getFollowingPosts(page)
          : this.postsService.getCommunityPosts(feed, page);

    request$.subscribe({
      next: (response) => {
        const mapped = response.content.map((post) => toPost(post));
        this.posts.update((list) => (page === 0 ? mapped : [...list, ...mapped]));
        this.lastPage.set(response.last);
        this.page.set(page);
        this.loadingMore.set(false);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch feed:', error);
        this.loadingMore.set(false);
        this.loading.set(false);
        this.toastService.error('Não foi possível carregar o feed.');
      },
    });
  }
}
