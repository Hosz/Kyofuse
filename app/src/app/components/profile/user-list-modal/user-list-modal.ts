import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { UserRowComponent } from '../../shared/user-row/user-row';
import { UserListEntry } from '../../../shared/models/profile.model';
import { FriendshipService } from '../../../core/services/friendship/friendship.service';
import { FollowService } from '../../../core/services/follow/follow.service';
import { PageResponse } from '../../../models/page-response.model';
import { friendshipResponse } from '../../../models/friendship/friendship-response.model';
import { followResponse } from '../../../models/follow/follow-response.model';

export type UserListType = 'followers' | 'following' | 'friends';

@Component({
  selector: 'app-user-list-modal',
  imports: [ModalComponent, UserRowComponent],
  templateUrl: './user-list-modal.html',
  styleUrl: './user-list-modal.css',
})
export class UserListModalComponent {
  open = input(false);
  title = input.required<string>();
  type = input.required<UserListType>();

  /** Quando omitido, mostra a lista do próprio usuário logado; quando informado,
   * mostra a lista desse outro usuário (perfil visitado). */
  profileUserId = input<string | null>(null);

  users = signal<UserListEntry[]>([]);
  loadingMore = signal(false);

  closed = output<void>();
  toggleFollow = output<UserListEntry>();

  private friendshipService = inject(FriendshipService);
  private followService = inject(FollowService);

  private loaded = false;
  private page = signal(0);
  private lastPage = signal(true);

  hasMoreToLoad = computed(() => !this.lastPage());

  constructor() {
    effect(() => {
      if (this.open() && !this.loaded) {
        this.loaded = true;
        this.fetchUsers(0);
      }
    });
  }

  loadMore(): void {
    if (this.loadingMore() || !this.hasMoreToLoad()) return;
    this.fetchUsers(this.page() + 1);
  }

  private fetchUsers(page: number): void {
    const otherUserId = this.profileUserId();
    this.loadingMore.set(true);

    switch (this.type()) {
      case 'followers':
        (otherUserId ? this.followService.showFollowers(otherUserId, page) : this.followService.showMyFollowers(page)).subscribe({
          next: (response) => this.appendEntries(page, this.toFollowEntries(response, 'followers'), response),
          error: (error) => {
            console.error('Failed to fetch followers list:', error);
            this.loadingMore.set(false);
          },
        });
        break;
      case 'following':
        (otherUserId ? this.followService.showFollowing(otherUserId, page) : this.followService.showMyFollowing(page)).subscribe({
          next: (response) => this.appendEntries(page, this.toFollowEntries(response, 'following'), response),
          error: (error) => {
            console.error('Failed to fetch following list:', error);
            this.loadingMore.set(false);
          },
        });
        break;
      case 'friends':
        (otherUserId ? this.friendshipService.showUserFriends(otherUserId, page) : this.friendshipService.showMyFriends(page)).subscribe({
          next: (response) => this.appendEntries(page, this.toFriendEntries(response), response),
          error: (error) => {
            console.error('Failed to fetch friends list:', error);
            this.loadingMore.set(false);
          },
        });
        break;
    }
  }

  private appendEntries(page: number, entries: UserListEntry[], response: PageResponse<unknown>): void {
    this.users.update((list) => (page === 0 ? entries : [...list, ...entries]));
    this.page.set(page);
    this.lastPage.set(response.last);
    this.loadingMore.set(false);
  }

  private toFollowEntries(
    response: PageResponse<followResponse>,
    type: 'followers' | 'following',
  ): UserListEntry[] {
    return response.content.map((entry) => ({
      id: type === 'followers' ? entry.followerId : entry.followedId,
      name: type === 'followers' ? entry.followerUsername : entry.followedUsername,
      handle: type === 'followers' ? entry.followerUsername : entry.followedUsername,
      avatarUrl: entry.avatarUrl,
    }));
  }

  private toFriendEntries(response: PageResponse<friendshipResponse>): UserListEntry[] {
    return response.content.map((entry) => ({
      id: entry.friendId,
      name: entry.friendUsername,
      handle: entry.friendUsername,
      avatarUrl: entry.avatarUrl,
    }));
  }
}
