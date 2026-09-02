import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { Router } from '@angular/router';
import { UserRowComponent } from '../../shared/user-row/user-row';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { SuggestedProfile } from '../../../shared/models/social.model';
import { FollowService } from '../../../core/services/follow/follow.service';
import { ToastService } from '../../../core/services/ui/toast.service';

export interface DisplaySuggestedUser {
  userId: string;
  name: string;
  handle: string;
  avatarUrl: string;
}

@Component({
  selector: 'app-suggested-follows',
  imports: [UserRowComponent],
  templateUrl: './suggested-follows.html',
  styleUrl: './suggested-follows.css',
})
export class SuggestedFollowsComponent implements OnInit {
  private followService = inject(FollowService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  customProfiles = input<SuggestedProfile[] | null>(null);

  profiles = signal<DisplaySuggestedUser[]>([]);
  loading = signal(true);
  loadingMore = signal(false);
  error = signal<string | null>(null);

  page = signal(0);
  totalPages = signal(0);

  follow = output<DisplaySuggestedUser>();

  private readonly followedUserIds = signal(new Set<string>());
  private readonly processingIds = signal(new Set<string>());

  ngOnInit(): void {
    const custom = this.customProfiles();
    if (custom && custom.length > 0) {
      this.profiles.set(
        custom.map((p, idx) => ({
          userId: p.id ?? `custom-${idx}`,
          name: p.name,
          handle: p.handle,
          avatarUrl: p.avatarUrl,
        }))
      );
      this.loading.set(false);
      return;
    }

    this.loadSuggestions();
  }

  isFollowing(userId: string): boolean {
    return this.followedUserIds().has(userId);
  }

  isProcessing(userId: string): boolean {
    return this.processingIds().has(userId);
  }

  onToggleFollow(profile: DisplaySuggestedUser): void {
    const userId = profile.userId;
    if (!userId || this.isProcessing(userId)) return;

    const alreadyFollowing = this.isFollowing(userId);
    const isCustom = userId.startsWith('custom-');

    this.followedUserIds.update((set) => {
      const next = new Set(set);
      if (alreadyFollowing) {
        next.delete(userId);
      } else {
        next.add(userId);
      }
      return next;
    });

    this.follow.emit(profile);

    if (isCustom) return;

    this.processingIds.update((set) => new Set(set).add(userId));

    if (alreadyFollowing) {
      this.followService.unfollowUser(userId).subscribe({
        next: () => {
          this.processingIds.update((set) => {
            const next = new Set(set);
            next.delete(userId);
            return next;
          });
        },
        error: (err) => {
          console.error('Failed to unfollow', err);
          this.followedUserIds.update((set) => new Set(set).add(userId));
          this.processingIds.update((set) => {
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
          this.processingIds.update((set) => {
            const next = new Set(set);
            next.delete(userId);
            return next;
          });
          this.toastService.success(`Você agora segue @${profile.handle}`);
        },
        error: (err) => {
          console.error('Failed to follow', err);
          this.followedUserIds.update((set) => {
            const next = new Set(set);
            next.delete(userId);
            return next;
          });
          this.processingIds.update((set) => {
            const next = new Set(set);
            next.delete(userId);
            return next;
          });
          this.toastService.error('Não foi possível seguir o usuário.');
        },
      });
    }
  }

  seeMore(): void {
    this.router.navigate(['/explorar'], { queryParams: { tab: 'profiles' } });
  }

  private mapProfileToDisplay(profile: gamerProfileResponse): DisplaySuggestedUser {
    return {
      userId: profile.userId,
      name: profile.nickname || profile.username,
      handle: profile.username,
      avatarUrl: profile.avatarUrl,
    };
  }

  private loadSuggestions(): void {
    this.loading.set(true);
    this.error.set(null);

    this.followService.getSuggestions(0, 3).subscribe({
      next: (response) => {
        this.profiles.set(response.content.map((p) => this.mapProfileToDisplay(p)));
        this.totalPages.set(response.totalPages);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load suggestions', err);
        this.error.set('Não foi possível carregar as sugestões.');
        this.loading.set(false);
      },
    });
  }

  private loadMoreSuggestions(): void {
    this.loadingMore.set(true);

    this.followService.getSuggestions(this.page(), 3).subscribe({
      next: (response) => {
        const newProfiles = response.content.map((p) => this.mapProfileToDisplay(p));
        this.profiles.update((prev) => [...prev, ...newProfiles]);
        this.totalPages.set(response.totalPages);
        this.loadingMore.set(false);
      },
      error: (err) => {
        console.error('Failed to load more suggestions', err);
        this.loadingMore.set(false);
      },
    });
  }
}