import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ModalComponent } from '../../shared/modal/modal';
import { FriendshipService } from '../../../core/services/friendship/friendship.service';
import { FollowService } from '../../../core/services/follow/follow.service';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';

export interface ChatCandidate {
  id: string;
  username: string;
  avatarUrl: string;
}

type ConversationMode = 'direct' | 'group';

/** Não existe endpoint de busca de usuários — a lista de candidatos vem só de quem
 * já tem relação com o usuário: amigos primeiro, depois quem segue e é seguido de volta. */
const CANDIDATE_SAMPLE_SIZE = 100;

@Component({
  selector: 'app-new-conversation-modal',
  imports: [ModalComponent],
  templateUrl: './new-conversation-modal.html',
  styleUrl: './new-conversation-modal.css',
})
export class NewConversationModalComponent {
  open = input(false);

  closed = output<void>();
  startDirect = output<string>();
  createGroup = output<{ name: string; avatarUrl?: string; participantIds: string[] }>();

  private friendshipService = inject(FriendshipService);
  private followService = inject(FollowService);

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  mode = signal<ConversationMode>('direct');
  query = signal('');
  groupName = signal('');
  groupAvatarUrl = signal('');
  loading = signal(false);

  private selectedIds = signal(new Set<string>());
  private friends = signal<ChatCandidate[]>([]);
  private mutualFollows = signal<ChatCandidate[]>([]);
  private loaded = false;

  candidates = computed<ChatCandidate[]>(() => {
    const friendIds = new Set(this.friends().map((c) => c.id));
    return [...this.friends(), ...this.mutualFollows().filter((c) => !friendIds.has(c.id))];
  });

  filteredCandidates = computed<ChatCandidate[]>(() => {
    const query = this.query().trim().toLowerCase();
    if (!query) return this.candidates();
    return this.candidates().filter((c) => c.username.toLowerCase().includes(query));
  });

  selectedCount = computed(() => this.selectedIds().size);
  canCreateGroup = computed(() => this.selectedCount() >= 2 && this.groupName().trim().length > 0);

  constructor() {
    effect(() => {
      if (this.open() && !this.loaded) {
        this.loaded = true;
        this.loadCandidates();
      }
    });
  }

  setMode(mode: ConversationMode): void {
    this.mode.set(mode);
    this.selectedIds.set(new Set());
  }

  isSelected(id: string): boolean {
    return this.selectedIds().has(id);
  }

  onQueryChange(value: string): void {
    this.query.set(value);
  }

  onGroupNameChange(value: string): void {
    this.groupName.set(value);
  }

  onGroupAvatarChange(value: string): void {
    this.groupAvatarUrl.set(value);
  }

  selectCandidate(candidate: ChatCandidate): void {
    if (this.mode() === 'direct') {
      this.startDirect.emit(candidate.id);
      this.reset();
      return;
    }

    this.selectedIds.update((ids) => {
      const next = new Set(ids);
      if (next.has(candidate.id)) {
        next.delete(candidate.id);
      } else {
        next.add(candidate.id);
      }
      return next;
    });
  }

  submitGroup(): void {
    if (!this.canCreateGroup()) return;
    this.createGroup.emit({
      name: this.groupName().trim(),
      avatarUrl: this.groupAvatarUrl().trim() || undefined,
      participantIds: [...this.selectedIds()],
    });
    this.reset();
  }

  onClose(): void {
    this.reset();
    this.closed.emit();
  }

  private reset(): void {
    this.mode.set('direct');
    this.query.set('');
    this.groupName.set('');
    this.groupAvatarUrl.set('');
    this.selectedIds.set(new Set());
  }

  private loadCandidates(): void {
    this.loading.set(true);

    this.friendshipService.showMyFriends(0, CANDIDATE_SAMPLE_SIZE).subscribe({
      next: (response) => {
        this.friends.set(
          response.content.map((friend) => ({
            id: friend.friendId,
            username: friend.friendUsername,
            avatarUrl: friend.avatarUrl || FALLBACK_AVATAR_URL,
          })),
        );
      },
      error: (error) => console.error('Failed to fetch friends:', error),
    });

    forkJoin({
      following: this.followService.showMyFollowing(0, CANDIDATE_SAMPLE_SIZE),
      followers: this.followService.showMyFollowers(0, CANDIDATE_SAMPLE_SIZE),
    }).subscribe({
      next: ({ following, followers }) => {
        const followerIds = new Set(
          followers.content.filter((f) => f.status === 'ACTIVE').map((f) => f.followerId),
        );
        this.mutualFollows.set(
          following.content
            .filter((f) => f.status === 'ACTIVE' && followerIds.has(f.followedId))
            .map((f) => ({
              id: f.followedId,
              username: f.followedUsername,
              avatarUrl: f.avatarUrl || FALLBACK_AVATAR_URL,
            })),
        );
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch mutual follows:', error);
        this.loading.set(false);
      },
    });
  }
}
