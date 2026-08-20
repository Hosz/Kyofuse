import { Component, computed, inject, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Comment } from '../../../shared/models/social.model';
import { ReactionButtonComponent } from '../../shared/reaction-button/reaction-button';
import { UserOptionsMenuComponent } from '../../shared/user-options-menu/user-options-menu';
import { ReactionSummaryComponent } from '../../shared/reaction-summary/reaction-summary';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';
import { CommentsService } from '../../../core/services/comments/comments.service';
import { CurrentUserService } from '../../../core/services/profile/current-user.service';
import { ToastService } from '../../../core/services/ui/toast.service';

@Component({
  selector: 'app-comment-card',
  imports: [RouterLink, ReactionButtonComponent, UserOptionsMenuComponent, ReactionSummaryComponent, ConfirmDialogComponent],
  templateUrl: './comment-card.html',
  styleUrl: './comment-card.css',
})
export class CommentCardComponent {
  comment = input.required<Comment>();
  /** Verdadeiro quando o usuário chegou aqui a partir de "Respostas" no perfil, pra destacar esse comentário. */
  highlighted = input(false);

  /** Emitido com o id do autor quando ele é bloqueado. */
  authorBlocked = output<string>();
  /** Emitido com o id do comentário depois que ele é apagado. */
  deleted = output<string>();

  private commentsService = inject(CommentsService);
  private currentUser = inject(CurrentUserService);
  private toastService = inject(ToastService);

  /** Só o autor apaga o próprio comentário. */
  isMine = computed(() => this.currentUser.isMe(this.comment().authorId));

  confirmDeleteOpen = signal(false);
  deleting = signal(false);

  askDelete(): void {
    this.confirmDeleteOpen.set(true);
  }

  cancelDelete(): void {
    if (this.deleting()) return;
    this.confirmDeleteOpen.set(false);
  }

  confirmDelete(): void {
    if (this.deleting()) return;
    this.deleting.set(true);

    const comment = this.comment();
    this.commentsService.deleteComment(comment.postId, comment.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.confirmDeleteOpen.set(false);
        this.toastService.success('Comentário apagado.');
        this.deleted.emit(comment.id);
      },
      error: (error) => {
        console.error('Failed to delete comment:', error);
        this.deleting.set(false);
        this.confirmDeleteOpen.set(false);
        this.toastService.error('Não foi possível apagar o comentário.');
      },
    });
  }
}
