import { Component, computed, input, output, signal } from '@angular/core';
import { Conversation } from '../../../shared/models/chat.model';
import { toChatMessageGroups } from '../../../shared/utils/mappers.util';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { ConversationResponse } from '../../../models/chat/chat.model';
import { ConversationInfoPanelComponent } from '../conversation-info-panel/conversation-info-panel';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';
import { ChatMessage } from '../../../shared/models/chat.model';

@Component({
  selector: 'app-chat-window',
  imports: [ConversationInfoPanelComponent, ConfirmDialogComponent],
  templateUrl: './chat-window.html',
  styleUrl: './chat-window.css',
})
export class ChatWindowComponent {
  conversation = input<Conversation | null>(null);
  myUserId = input<string | null>(null);

  accept = output<void>();
  decline = output<void>();
  revoke = output<void>();
  sendMessage = output<string>();
  deleteMessage = output<string>();
  groupUpdated = output<ConversationResponse>();

  draft = signal('');
  /** Mensagem esperando confirmação de exclusão — apagar não dá pra desfazer. */
  messagePendingDeletion = signal<ChatMessage | null>(null);
  menuOpen = signal(false);
  infoPanelOpen = signal(false);

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  messageGroups = computed(() => toChatMessageGroups(this.conversation()?.messages ?? []));

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
    // Quem recebeu o pedido não pode mandar nada até aceitar — o backend recusa
    // (só quem criou a conversa pode mandar a mensagem única enquanto PENDING).
    if (conversation.relationship === 'request-received') return false;
    if (conversation.relationship === 'request-sent' && this.hasSentPendingMessage()) return false;
    return true;
  });

  onDraftChange(value: string): void {
    this.draft.set(value);
  }

  onSend(): void {
    const content = this.draft().trim();
    if (!content || !this.canType()) return;
    this.sendMessage.emit(content);
    this.draft.set('');
  }
}
