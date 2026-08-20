import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { ModalComponent } from '../modal/modal';
import { UserRowComponent } from '../user-row/user-row';
import { PostReactionService } from '../../../core/services/reactions/post-reaction.service';
import { CommentReactionService } from '../../../core/services/reactions/comment-reaction.service';
import { PageResponse } from '../../../models/page-response.model';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { REACTION_OPTIONS, ReactionType } from '../../../shared/models/reaction.model';

interface ReactionEntry {
  userId: string;
  username: string;
  nickname: string;
  avatarUrl: string;
  reactionType: ReactionType;
}

@Component({
  selector: 'app-reaction-list-modal',
  imports: [ModalComponent, UserRowComponent],
  templateUrl: './reaction-list-modal.html',
  styleUrl: './reaction-list-modal.css',
})
export class ReactionListModalComponent {
  open = input(false);
  postId = input.required<string>();
  /** Ausente quando a reação é de um post; presente quando é de um comentário. */
  commentId = input<string | null>(null);

  closed = output<void>();

  private postReactionService = inject(PostReactionService);
  private commentReactionService = inject(CommentReactionService);

  entries = signal<ReactionEntry[]>([]);
  loading = signal(false);
  loadingMore = signal(false);
  error = signal<string | null>(null);

  private page = signal(0);
  private lastPage = signal(true);

  hasMoreToLoad = computed(() => !this.lastPage());

  constructor() {
    effect(() => {
      if (this.open()) this.fetch(0);
    });
  }

  loadMore(): void {
    if (this.loadingMore() || !this.hasMoreToLoad()) return;
    this.fetch(this.page() + 1);
  }

  reactionIcon(type: ReactionType): string {
    return REACTION_OPTIONS.find((option) => option.type === type)?.icon ?? 'favorite';
  }

  reactionLabel(type: ReactionType): string {
    return REACTION_OPTIONS.find((option) => option.type === type)?.label ?? type;
  }

  /** Uma listagem só: getReactions devolve curtidas e demais reações juntas, e o tipo
   * de cada uma aparece no ícone ao lado da pessoa. */
  private fetch(page: number): void {
    if (page === 0) {
      this.loading.set(true);
      this.error.set(null);
    } else {
      this.loadingMore.set(true);
    }

    const commentId = this.commentId();
    const request$ = commentId
      ? this.commentReactionService.getReactions(this.postId(), commentId, page)
      : this.postReactionService.getReactions(this.postId(), page);

    request$.subscribe({
      next: (response) => this.applyResponse(page, response),
      error: (error) => {
        console.error('Failed to fetch reaction list:', error);
        this.error.set('Não foi possível carregar a lista.');
        this.loading.set(false);
        this.loadingMore.set(false);
      },
    });
  }

  private applyResponse(page: number, response: PageResponse<{ userId: string; username: string; nickname: string; profileImage: string; reactionType: ReactionType }>): void {
    const mapped = response.content.map((entry) => ({
      userId: entry.userId,
      username: entry.username,
      nickname: entry.nickname,
      avatarUrl: entry.profileImage || FALLBACK_AVATAR_URL,
      reactionType: entry.reactionType,
    }));

    this.entries.update((list) => (page === 0 ? mapped : [...list, ...mapped]));
    this.page.set(page);
    this.lastPage.set(response.last);
    this.loading.set(false);
    this.loadingMore.set(false);
  }
}
