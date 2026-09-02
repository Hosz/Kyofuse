import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { UserRowComponent } from '../../shared/user-row/user-row';
import { UserOptionsMenuComponent } from '../../shared/user-options-menu/user-options-menu';
import { UserListEntry } from '../../../shared/models/profile.model';
import { FriendshipService } from '../../../core/services/friendship/friendship.service';
import { FollowService } from '../../../core/services/follow/follow.service';
import { PageResponse } from '../../../models/page-response.model';
import { friendshipResponse } from '../../../models/friendship/friendship-response.model';
import { followResponse } from '../../../models/follow/follow-response.model';

export type UserListType = 'followers' | 'following' | 'friends';

@Component({
  selector: 'app-user-list-modal',
  imports: [ModalComponent, UserRowComponent, UserOptionsMenuComponent],
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

  /** Só na lista de seguidores do próprio dono: libera remover e bloquear. */
  manageable = input(false);

  users = signal<UserListEntry[]>([]);
  loadingMore = signal(false);

  /** 403 aqui é a resposta esperada num perfil privado de quem não segue — vira aviso
   * na tela em vez de lista vazia sem explicação. */
  restricted = signal(false);

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
      if (this.open()) {
        if (!this.loaded) {
          this.loaded = true;
          this.fetchUsers(0);
        }
      } else {
        this.loaded = false;
        this.users.set([]);
        this.page.set(0);
        this.lastPage.set(true);
        this.restricted.set(false);
      }
    });
  }

  canManageFollowers = computed(() => this.manageable() && this.type() === 'followers');
  canUnfollow = computed(() => this.manageable() && this.type() === 'following');
  canRemoveFriend = computed(() => this.manageable() && this.type() === 'friends');
  hasRowActions = computed(() => this.canManageFollowers() || this.canUnfollow() || this.canRemoveFriend());

  /** Some da lista na hora — a pessoa deixou de ser seguidora. */
  removeFromList(userId: string): void {
    this.users.update((list) => list.filter((user) => user.id !== userId));
  }

  loadMore(): void {
    if (this.loadingMore() || !this.hasMoreToLoad()) return;
    this.fetchUsers(this.page() + 1);
  }

  private fetchUsers(page: number): void {
    const isOwner = this.manageable();
    const otherUserId = isOwner ? null : this.profileUserId();
    this.loadingMore.set(true);

    switch (this.type()) {
      case 'followers':
        (!otherUserId ? this.followService.showMyFollowers(page) : this.followService.showFollowers(otherUserId, page)).subscribe({
          next: (response) => this.appendEntries(page, this.toFollowEntries(response, 'followers'), response),
          error: (error) => {
            if (error?.status === 403) {
              this.restricted.set(true);
            } else {
              console.error('Failed to fetch followers list:', error);
            }
            this.loadingMore.set(false);
          },
        });
        break;
      case 'following':
        (!otherUserId ? this.followService.showMyFollowing(page) : this.followService.showFollowing(otherUserId, page)).subscribe({
          next: (response) => this.appendEntries(page, this.toFollowEntries(response, 'following'), response),
          error: (error) => {
            if (error?.status === 403) {
              this.restricted.set(true);
            } else {
              console.error('Failed to fetch following list:', error);
            }
            this.loadingMore.set(false);
          },
        });
        break;
      case 'friends':
        (!otherUserId ? this.friendshipService.showMyFriends(page) : this.friendshipService.showUserFriends(otherUserId, page)).subscribe({
          next: (response) => this.appendEntries(page, this.toFriendEntries(response), response),
          error: (error) => {
            if (error?.status === 403) {
              this.restricted.set(true);
            } else {
              console.error('Failed to fetch friends list:', error);
            }
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
