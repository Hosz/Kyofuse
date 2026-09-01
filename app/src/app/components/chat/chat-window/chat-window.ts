import { Component, ElementRef, ViewChild, afterRenderEffect, computed, inject, input, output, signal, untracked, viewChild } from '@angular/core';
import { Conversation } from '../../../shared/models/chat.model';
import { toChatMessageGroups } from '../../../shared/utils/mappers.util';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { ConversationResponse, MessageInfoResponse } from '../../../models/chat/chat.model';
import { ConversationInfoPanelComponent } from '../conversation-info-panel/conversation-info-panel';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';
import { ImageModalComponent } from '../../shared/image-modal/image-modal';
import { MessageInfoModalComponent } from '../message-info-modal/message-info-modal';
import { ChatMessage } from '../../../shared/models/chat.model';
import { MediaService } from '../../../core/services/media/media.service';
import { MessageService } from '../../../core/services/chat/message.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { PostMediaItemRequest } from '../../../models/posts/post-request.model';

@Component({
  selector: 'app-chat-window',
  imports: [ConversationInfoPanelComponent, ConfirmDialogComponent, ImageModalComponent, MessageInfoModalComponent],
  templateUrl: './chat-window.html',
  styleUrl: './chat-window.css',
})
export class ChatWindowComponent {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;
  private scroller = viewChild<ElementRef<HTMLElement>>('scroller');

  private toastService = inject(ToastService);
  private mediaService = inject(MediaService);
  private messageService = inject(MessageService);

  conversation = input<Conversation | null>(null);
  myUserId = input<string | null>(null);
  loadingOlderMessages = input(false);

  comingSoon(feature: string): void {
    this.toastService.info(`${feature} estará disponível em breve!`);
  }

  accept = output<void>();
  decline = output<void>();
  revoke = output<void>();
  allow = output<void>();
  sendMessage = output<{ content?: string; media?: PostMediaItemRequest[] }>();
  deleteMessage = output<string>();
  groupUpdated = output<ConversationResponse>();
  loadOlderMessages = output<void>();

  draft = signal('');
  pendingMedia = signal<PostMediaItemRequest[]>([]);
  uploadingMedia = signal(false);
  selectedImageUrl = signal<string | null>(null);

  /** Mensagem esperando confirmação de exclusão — apagar não dá pra desfazer. */
  messagePendingDeletion = signal<ChatMessage | null>(null);
  menuOpen = signal(false);
  infoPanelOpen = signal(false);

  /** Modal de detalhes de entrega e leitura da mensagem. */
  infoModalOpen = signal(false);
  messageInfo = signal<MessageInfoResponse | null>(null);
  loadingMessageInfo = signal(false);

  /** Botão flutuante para descer a tela quando o usuário rolou pra cima. */
  showScrollBottom = signal(false);
  private lastMessageCount = 0;
  private currentConvId: string | null = null;
  private previousScrollHeight: number | null = null;
  private previousScrollTop: number | null = null;

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  messageGroups = computed(() =>
    toChatMessageGroups(this.conversation()?.messages ?? [], this.conversation()?.unreadDividerMessageId),
  );

  constructor() {
    afterRenderEffect({
      write: () => {
        this.messageGroups();
        const conv = this.conversation();
        const element = this.scroller()?.nativeElement;
        if (!element) return;

        const convChanged = conv?.id !== this.currentConvId;
        this.currentConvId = conv?.id ?? null;

        const totalMessages = conv?.messages.length ?? 0;
        const hasNewMessages = totalMessages > this.lastMessageCount;
        this.lastMessageCount = totalMessages;

        if (this.previousScrollHeight !== null && this.previousScrollTop !== null) {
          const heightDiff = element.scrollHeight - this.previousScrollHeight;
          element.scrollTop = this.previousScrollTop + heightDiff;
          this.previousScrollHeight = null;
          this.previousScrollTop = null;
        } else if (convChanged) {
          const unreadDividerEl = element.querySelector('#unread-divider') as HTMLElement | null;
          if (unreadDividerEl) {
            unreadDividerEl.scrollIntoView({ block: 'start', behavior: 'instant' });
            this.showScrollBottom.set(true);
          } else {
            element.scrollTop = element.scrollHeight;
            if (untracked(() => this.showScrollBottom())) {
              this.showScrollBottom.set(false);
            }
          }
        } else if (hasNewMessages) {
          // Chegou nova mensagem: se fui eu quem enviou ou se já estava no fim, rola suavemente
          const isMe = conv?.messages.at(-1)?.author === 'me';
          const isNearBottom = element.scrollHeight - element.scrollTop - element.clientHeight < 150;
          if (isMe || isNearBottom) {
            element.scrollTo({
              top: element.scrollHeight,
              behavior: 'smooth',
            });
            if (untracked(() => this.showScrollBottom())) {
              this.showScrollBottom.set(false);
            }
          }
        }
      },
    });
  }

  openImage(url: string): void {
    this.selectedImageUrl.set(url);
  }

  closeImage(): void {
    this.selectedImageUrl.set(null);
  }

  openMessageInfo(messageId: string): void {
    const conv = this.conversation();
    if (!conv) return;

    this.infoModalOpen.set(true);
    this.loadingMessageInfo.set(true);
    this.messageInfo.set(null);

    this.messageService.getMessageInfo(conv.id, messageId).subscribe({
      next: (res) => {
        this.messageInfo.set(res);
        this.loadingMessageInfo.set(false);
      },
      error: (err) => {
        console.error('Failed to load message info:', err);
        this.toastService.error('Não foi possível carregar os dados da mensagem.');
        this.loadingMessageInfo.set(false);
      },
    });
  }

  closeMessageInfo(): void {
    this.infoModalOpen.set(false);
    this.messageInfo.set(null);
  }

  triggerFileInput(): void {
    if (this.uploadingMedia()) return;
    this.fileInput?.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    this.uploadingMedia.set(true);

    this.mediaService.uploadImage(file).subscribe({
      next: (res) => {
        const item: PostMediaItemRequest = {
          fileKey: res.fileKey,
          url: res.url,
          thumbnailUrl: res.thumbnailUrl,
          contentType: res.contentType,
          fileSizeBytes: res.fileSizeBytes,
          width: res.width,
          height: res.height,
        };
        this.pendingMedia.update((items) => [...items, item]);
        this.uploadingMedia.set(false);
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      },
      error: (err) => {
        console.error('Failed to upload image:', err);
        this.toastService.error('Erro ao enviar imagem.');
        this.uploadingMedia.set(false);
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      },
    });
  }

  removePendingMedia(index: number): void {
    this.pendingMedia.update((items) => items.filter((_, i) => i !== index));
  }

  askDeleteMessage(message: ChatMessage): void {
    this.messagePendingDeletion.set(message);
  }

  cancelDeleteMessage(): void {
    this.messagePendingDeletion.set(null);
  }

  confirmDeleteMessage(): void {
    const message = this.messagePendingDeletion();
    if (!message) return;
    this.messagePendingDeletion.set(null);
    this.deleteMessage.emit(message.id);
  }

  openInfoPanel(): void {
    this.infoPanelOpen.set(true);
  }

  closeInfoPanel(): void {
    this.infoPanelOpen.set(false);
  }

  canRevoke = computed(() => {
    const conversation = this.conversation();
    return !!conversation && conversation.type === 'DIRECT' && conversation.relationship === 'mutual';
  });

  canAllow = computed(() => {
    const conversation = this.conversation();
    if (!conversation || conversation.type !== 'DIRECT' || conversation.relationship !== 'declined') {
      return false;
    }
    const myId = this.myUserId();
    return !conversation.revokedById || conversation.revokedById === myId;
  });

  toggleMenu(): void {
    this.menuOpen.update((value) => !value);
  }

  onRevoke(): void {
    this.menuOpen.set(false);
    this.revoke.emit();
  }

  onAllow(): void {
    this.menuOpen.set(false);
    this.allow.emit();
  }

  hasSentPendingMessage = computed(() => {
    const conversation = this.conversation();
    if (!conversation || conversation.relationship !== 'request-sent') return false;
    return conversation.messages.some((message) => message.author === 'me');
  });

  canType = computed(() => {
    const conversation = this.conversation();
    if (!conversation) return false;
    if (conversation.relationship === 'declined') return false;
    if (conversation.relationship === 'request-received') return false;
    if (conversation.relationship === 'request-sent' && this.hasSentPendingMessage()) return false;
    return true;
  });

  onScroll(): void {
    const element = this.scroller()?.nativeElement;
    if (!element) return;
    const isScrolledUp = element.scrollHeight - element.scrollTop - element.clientHeight > 200;
    this.showScrollBottom.set(isScrolledUp);

    const conv = this.conversation();
    if (element.scrollTop < 80 && conv?.hasMoreMessages && !this.loadingOlderMessages()) {
      this.previousScrollHeight = element.scrollHeight;
      this.previousScrollTop = element.scrollTop;
      this.loadOlderMessages.emit();
    }
  }

  scrollToBottom(behavior: ScrollBehavior = 'smooth'): void {
    const element = this.scroller()?.nativeElement;
    if (element) {
      element.scrollTo({
        top: element.scrollHeight,
        behavior,
      });
      this.showScrollBottom.set(false);
    }
  }

  onDraftChange(value: string): void {
    this.draft.set(value);
  }

  onSend(): void {
    const content = this.draft().trim();
    const media = this.pendingMedia();
    if ((!content && media.length === 0) || !this.canType() || this.uploadingMedia()) return;

    this.sendMessage.emit({
      content: content || undefined,
      media: media.length > 0 ? media : undefined,
    });
    this.draft.set('');
    this.pendingMedia.set([]);

    // Força rolagem imediata e suave para a nova mensagem enviada
    setTimeout(() => {
      this.scrollToBottom('smooth');
    }, 50);
  }
}
