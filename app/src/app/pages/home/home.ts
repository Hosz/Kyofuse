import { Component, computed, inject, signal } from '@angular/core';
import { Post } from '../../shared/models/social.model';
import { DiscoverySidebarComponent } from '../../components/discovery/discovery-sidebar/discovery-sidebar';
import { PostComposerComponent } from '../../components/feed/post-composer/post-composer';
import { FeedTabsComponent } from '../../components/feed/feed-tabs/feed-tabs';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { FeedListComponent } from '../../components/feed/feed-list/feed-list';
import { PostsService } from '../../core/services/posts/posts.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { toPost } from '../../shared/utils/mappers.util';

@Component({
  selector: 'app-home',
  imports: [
    AppSidebarComponent,
    FeedTabsComponent,
    PostComposerComponent,
    FeedListComponent,
    DiscoverySidebarComponent
  ],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent {

  postsService = inject(PostsService);
  userService = inject(ProfileService);
  posts = signal<Post[]>([]);

  private page = signal(0);
  private lastPage = signal(true);
  loadingMore = signal(false);
  hasMoreToLoad = computed(() => !this.lastPage());

  ngOnInit() {
    this.loadFeed(0);
  }

  loadMore(): void {
    if (this.loadingMore() || this.lastPage()) return;
    this.loadingMore.set(true);
    this.loadFeed(this.page() + 1);
  }

  onAuthorBlocked(authorId: string): void {
    this.posts.update((list) => list.filter((post) => post.author.id !== authorId));
  }

  private loadFeed(page: number): void {
    this.postsService.getFeed(page).subscribe({
      next: (response) => {
        const mapped = response.content.map((post) => toPost(post));
        this.posts.update((list) => (page === 0 ? mapped : [...list, ...mapped]));
        this.lastPage.set(response.last);
        this.page.set(page);
        this.loadingMore.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch feed:', error);
        this.loadingMore.set(false);
      },
    });
  }
}
