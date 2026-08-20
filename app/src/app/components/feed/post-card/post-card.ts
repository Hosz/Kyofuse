import { Component, computed, inject, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Post } from '../../../shared/models/social.model';
import { ReactionButtonComponent } from '../../shared/reaction-button/reaction-button';
import { UserOptionsMenuComponent } from '../../shared/user-options-menu/user-options-menu';
import { ShareButtonComponent } from '../../shared/share-button/share-button';
import { ReactionSummaryComponent } from '../../shared/reaction-summary/reaction-summary';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';
import { PostsService } from '../../../core/services/posts/posts.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { CurrentUserService } from '../../../core/services/profile/current-user.service';

@Component({
  selector: 'app-post-card',
  imports: [RouterLink, ReactionButtonComponent, UserOptionsMenuComponent, ShareButtonComponent, ReactionSummaryComponent, ConfirmDialogComponent],
  templateUrl: './post-card.html',
  styleUrl: './post-card.css',
})
export class PostCardComponent {
  post = input.required<Post>();

  /** Emitido com o id do autor quando ele é bloqueado, pra quem estiver ouvindo remover os posts dele da lista. */
  authorBlocked = output<string>();
  /** Emitido com o id do post depois que ele é apagado. */
  deleted = output<string>();

  private postsService = inject(PostsService);
  private currentUser = inject(CurrentUserService);
  private toastService = inject(ToastService);

  /** Só o autor apaga o próprio post — mesma regra do backend. */
  canDelete = computed(() => this.currentUser.isMe(this.post().author.id));

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

    const postId = this.post().id;
    this.postsService.deletePost(postId).subscribe({
      next: () => {
        this.deleting.set(false);
        this.confirmDeleteOpen.set(false);
        this.toastService.success('Publicação apagada.');
        this.deleted.emit(postId);
      },
      error: (error) => {
        console.error('Failed to delete post:', error);
        this.deleting.set(false);
        this.confirmDeleteOpen.set(false);
        this.toastService.error('Não foi possível apagar a publicação.');
      },
    });
  }
}