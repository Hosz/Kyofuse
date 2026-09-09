import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { PostReactionService } from '../../../core/services/reactions/post-reaction.service';
import { CommentReactionService } from '../../../core/services/reactions/comment-reaction.service';
import { REACTION_OPTIONS, ReactionType } from '../../../shared/models/reaction.model';
import { ReactionListModalComponent } from '../reaction-list-modal/reaction-list-modal';

@Component({
  selector: 'app-reaction-button',
  imports: [ReactionListModalComponent],
  templateUrl: './reaction-button.html',
  styleUrl: './reaction-button.css',
})
export class ReactionButtonComponent {
  postId = input.required<string>();
  commentId = input<string | null>(null);
  likeCount = input.required<number>();
  reactionCount = input.required<number>();
  currentUserReaction = input<ReactionType | string | null | undefined>(null);

  private postReactionService = inject(PostReactionService);
  private commentReactionService = inject(CommentReactionService);

  readonly options = REACTION_OPTIONS;

  myReaction = signal<ReactionType | null>(null);
  pickerOpen = signal(false);
  pending = signal(false);

  constructor() {
    effect(() => {
      const initial = (this.currentUserReaction() as ReactionType) ?? null;
      this.myReaction.set(initial);
    });
  }

  /** Atraso pra fechar o seletor: sem isso, o mouse "sai" do wrapper ao cruzar
   * o espaço vazio entre o botão e o popup (que fica fora do fluxo normal) antes
   * de conseguir alcançá-lo, fechando o seletor antes do usuário clicar. */
  private closeTimer: ReturnType<typeof setTimeout> | undefined;

  activeOption = computed(() => this.options.find((option) => option.type === this.myReaction()) ?? null);

  private initialReaction = computed(() => (this.currentUserReaction() as ReactionType) ?? null);

  private baseLikeCount = computed(() => {
    const init = this.initialReaction();
    return Math.max(0, this.likeCount() - (init === 'LIKE' ? 1 : 0));
  });

  private baseReactionCount = computed(() => {
    const init = this.initialReaction();
    return Math.max(0, this.reactionCount() - (init && init !== 'LIKE' ? 1 : 0));
  });

  displayLikeCount = computed(() => this.baseLikeCount() + (this.myReaction() === 'LIKE' ? 1 : 0));
  displayReactionCount = computed(() =>
    this.baseReactionCount() + (this.myReaction() && this.myReaction() !== 'LIKE' ? 1 : 0),
  );
  totalCount = computed(() => this.displayLikeCount() + this.displayReactionCount());

  listModalOpen = signal(false);

  openList(): void {
    this.listModalOpen.set(true);
  }

  closeList(): void {
    this.listModalOpen.set(false);
  }

  openPicker(): void {
    this.clearCloseTimer();
    this.pickerOpen.set(true);
  }

  scheduleClosePicker(): void {
    this.clearCloseTimer();
    this.closeTimer = setTimeout(() => this.pickerOpen.set(false), 250);
  }

  togglePicker(): void {
    this.clearCloseTimer();
    this.pickerOpen.set(!this.pickerOpen());
  }

  private clearCloseTimer(): void {
    if (this.closeTimer !== undefined) {
      clearTimeout(this.closeTimer);
      this.closeTimer = undefined;
    }
  }

  quickToggle(): void {
    if (this.myReaction()) {
      this.remove();
    } else {
      this.select('LIKE');
    }
  }

  select(type: ReactionType): void {
    if (this.pending()) return;
    this.clearCloseTimer();
    this.pickerOpen.set(false);

    if (this.myReaction() === type) {
      this.remove();
      return;
    }

    const previous = this.myReaction();
    this.myReaction.set(type);
    this.pending.set(true);

    this.upsert(type).subscribe({
      next: () => this.pending.set(false),
      error: (error) => {
        console.error('Failed to react:', error);
        this.myReaction.set(previous);
        this.pending.set(false);
      },
    });
  }

  private remove(): void {
    if (this.pending()) return;

    const previous = this.myReaction();
    this.myReaction.set(null);
    this.pending.set(true);

    const commentId = this.commentId();
    const request$ = commentId
      ? this.commentReactionService.removeCommentReaction(this.postId(), commentId)
      : this.postReactionService.removeReaction(this.postId());

    request$.subscribe({
      next: () => this.pending.set(false),
      error: (error) => {
        console.error('Failed to remove reaction:', error);
        this.myReaction.set(previous);
        this.pending.set(false);
      },
    });
  }

  private upsert(type: ReactionType) {
    const commentId = this.commentId();
    return commentId
      ? this.commentReactionService.upsertCommentReaction(this.postId(), commentId, { reactionType: type })
      : this.postReactionService.upsertPostReaction(this.postId(), { reactionType: type });
  }
}
