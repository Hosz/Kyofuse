import { Component, ElementRef, ViewChild, computed, inject, input, output, signal } from '@angular/core';
import { Conversation } from '../../../shared/models/chat.model';
import { toChatMessageGroups } from '../../../shared/utils/mappers.util';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { ConversationResponse } from '../../../models/chat/chat.model';
import { ConversationInfoPanelComponent } from '../conversation-info-panel/conversation-info-panel';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';
import { ImageModalComponent } from '../../shared/image-modal/image-modal';
import { ChatMessage } from '../../../shared/models/chat.model';
import { MediaService } from '../../../core/services/media/media.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { PostMediaItemRequest } from '../../../models/posts/post-request.model';

@Component({
  selector: 'app-chat-window',
  imports: [ConversationInfoPanelComponent, ConfirmDialogComponent, ImageModalComponent],
  templateUrl: './chat-window.html',
  styleUrl: './chat-window.css',
})
export class ChatWindowComponent {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  private toastService = inject(ToastService);
  private mediaService = inject(MediaService);

  conversation = input<Conversation | null>(null);
  myUserId = input<string | null>(null);

  comingSoon(feature: string): void {
    this.toastService.info(`${feature} estará disponível em breve!`);
  }

  accept = output<void>();
  decline = output<void>();
  revoke = output<void>();
  sendMessage = output<{ content?: string; media?: PostMediaItemRequest[] }>();
  deleteMessage = output<string>();
  groupUpdated = output<ConversationResponse>();

  draft = signal('');
  pendingMedia = signal<PostMediaItemRequest[]>([]);
  uploadingMedia = signal(false);
  selectedImageUrl = signal<string | null>(null);

  /** Mensagem esperando confirmação de exclusão — apagar não dá pra desfazer. */
  messagePendingDeletion = signal<ChatMessage | null>(null);
  menuOpen = signal(false);
  infoPanelOpen = signal(false);

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  messageGroups = computed(() => toChatMessageGroups(this.conversation()?.messages ?? []));

  openImage(url: string): void {
    this.selectedImageUrl.set(url);
  }

  closeImage(): void {
    this.selectedImageUrl.set(null);
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

  toggleMenu(): void {
    this.menuOpen.update((value) => !value);
  }

  onRevoke(): void {
    this.menuOpen.set(false);
    this.revoke.emit();
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
  }
}
