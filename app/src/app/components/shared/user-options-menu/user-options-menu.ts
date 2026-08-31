import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';
import { ModalComponent } from '../modal/modal';
import { BlockService } from '../../../core/services/block/block.service';
import { FollowService } from '../../../core/services/follow/follow.service';
import { FriendshipService } from '../../../core/services/friendship/friendship.service';
import { CurrentUserService } from '../../../core/services/profile/current-user.service';

@Component({
  selector: 'app-user-options-menu',
  imports: [ModalComponent, ConfirmDialogComponent, NgTemplateOutlet],
  templateUrl: './user-options-menu.html',
  styleUrl: './user-options-menu.css',
})
export class UserOptionsMenuComponent {
  userId = input.required<string>();

  /**
   * 'dropdown' abre as opções num popover absoluto ao lado do botão — serve quando o
   * menu está numa página comum. 'modal' abre as opções como um modal próprio,
   * empilhado por cima do que já estiver aberto: necessário quando este menu vive
   * dentro de outro modal, onde um popover absoluto ficaria cortado pelo contêiner
   * de rolagem do modal pai.
   */
  presentation = input<'dropdown' | 'modal'>('dropdown');

  /** Só faz sentido na lista dos meus próprios seguidores. */
  canRemoveFollower = input(false);
  /** Na lista de quem eu sigo. */
  canUnfollow = input(false);
  /** Na lista de amigos. */
  canRemoveFriend = input(false);
  /** Desligado nos meus próprios conteúdos: não faz sentido me bloquear ou me denunciar. */
  canBlock = input(true);
  /** Ações sobre o próprio conteúdo. */
  canEditContent = input(false);
  /** Quando preenchido, "Editar" aparece desabilitado com esse motivo no title. */
  editDisabledReason = input<string | null>(null);
  canDeleteContent = input(false);
  /** Rótulo do que está sendo apagado ("publicação", "comentário"). */
  contentLabel = input('publicação');

  /** Emitido depois que o bloqueio é confirmado com sucesso no backend. */
  blocked = output<void>();
  /** Emitido depois que o seguidor é removido com sucesso. */
  followerRemoved = output<void>();
  /** Emitido depois de deixar de seguir. */
  unfollowed = output<void>();
  /** Emitido depois de desfazer a amizade. */
  friendRemoved = output<void>();
  /** O dono do conteúdo trata a edição e a exclusão — o menu só dispara. */
  editContent = output<void>();
  deleteContent = output<void>();

  private blockService = inject(BlockService);
  private followService = inject(FollowService);
  private friendshipService = inject(FriendshipService);
  private currentUser = inject(CurrentUserService);

  effectiveCanBlock = computed(() => this.canBlock() && !this.currentUser.isMe(this.userId()));

  menuOpen = signal(false);
  confirmOpen = signal(false);
  blocking = signal(false);
  error = signal<string | null>(null);

  requestEdit(): void {
    this.closeMenu();
    this.editContent.emit();
  }

  requestDelete(): void {
    this.closeMenu();
    this.deleteContent.emit();
  }

  toggleMenu(): void {
    this.menuOpen.set(!this.menuOpen());
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  openBlockConfirm(): void {
    this.closeMenu();
    this.error.set(null);
    this.confirmOpen.set(true);
  }

  closeBlockConfirm(): void {
    if (this.blocking()) return;
    this.confirmOpen.set(false);
  }

  removeConfirmOpen = signal(false);
  removing = signal(false);
  removeError = signal<string | null>(null);

  openRemoveConfirm(): void {
    this.closeMenu();
    this.removeError.set(null);
    this.removeConfirmOpen.set(true);
  }

  closeRemoveConfirm(): void {
    if (this.removing()) return;
    this.removeConfirmOpen.set(false);
  }

  confirmRemoveFollower(): void {
    if (this.removing()) return;
    this.removing.set(true);
    this.removeError.set(null);

    this.followService.removeFollower(this.userId()).subscribe({
      next: () => {
        this.removing.set(false);
        this.removeConfirmOpen.set(false);
        this.followerRemoved.emit();
      },
      error: (error) => {
        console.error('Failed to remove follower:', error);
        this.removing.set(false);
        this.removeError.set('Não foi possível remover esse seguidor. Tente novamente.');
      },
    });
  }

  unfollowConfirmOpen = signal(false);
  unfollowing = signal(false);
  unfollowError = signal<string | null>(null);

  openUnfollowConfirm(): void {
    this.closeMenu();
    this.unfollowError.set(null);
    this.unfollowConfirmOpen.set(true);
  }

  closeUnfollowConfirm(): void {
    if (this.unfollowing()) return;
    this.unfollowConfirmOpen.set(false);
  }

  confirmUnfollow(): void {
    if (this.unfollowing()) return;
    this.unfollowing.set(true);
    this.unfollowError.set(null);

    this.followService.unfollowUser(this.userId()).subscribe({
      next: () => {
        this.unfollowing.set(false);
        this.unfollowConfirmOpen.set(false);
        this.unfollowed.emit();
      },
      error: (error) => {
        console.error('Failed to unfollow user:', error);
        this.unfollowing.set(false);
        this.unfollowError.set('Não foi possível deixar de seguir. Tente novamente.');
      },
    });
  }

  friendConfirmOpen = signal(false);
  removingFriend = signal(false);
  friendError = signal<string | null>(null);

  openFriendConfirm(): void {
    this.closeMenu();
    this.friendError.set(null);
    this.friendConfirmOpen.set(true);
  }

  closeFriendConfirm(): void {
    if (this.removingFriend()) return;
    this.friendConfirmOpen.set(false);
  }

  confirmRemoveFriend(): void {
    if (this.removingFriend()) return;
    this.removingFriend.set(true);
    this.friendError.set(null);

    this.friendshipService.removeFriendship(this.userId()).subscribe({
      next: () => {
        this.removingFriend.set(false);
        this.friendConfirmOpen.set(false);
        this.friendRemoved.emit();
      },
      error: (error) => {
        console.error('Failed to remove friend:', error);
        this.removingFriend.set(false);
        this.friendError.set('Não foi possível desfazer a amizade. Tente novamente.');
      },
    });
  }

  confirmBlock(): void {
    if (this.blocking()) return;
    this.blocking.set(true);
    this.error.set(null);

    this.blockService.blockUser(this.userId()).subscribe({
      next: () => {
        this.blocking.set(false);
        this.confirmOpen.set(false);
        this.blocked.emit();
      },
      error: (error) => {
        console.error('Failed to block user:', error);
        this.blocking.set(false);
        this.error.set('Não foi possível bloquear esse usuário. Tente novamente.');
      },
    });
  }
}
