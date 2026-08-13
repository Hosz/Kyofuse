import { Component, computed, input, output, signal } from '@angular/core';
import { Conversation } from '../../../shared/models/chat.model';

@Component({
  selector: 'app-chat-window',
  imports: [],
  templateUrl: './chat-window.html',
  styleUrl: './chat-window.css',
})
export class ChatWindowComponent {
  conversation = input<Conversation | null>(null);

  accept = output<void>();
  decline = output<void>();
  sendMessage = output<string>();

  draft = signal('');

  hasSentPendingMessage = computed(() => {
    const conversation = this.conversation();
    if (!conversation || conversation.relationship !== 'request-sent') return false;
    return conversation.messages.some((message) => message.author === 'me');
  });

  canType = computed(() => {
    const conversation = this.conversation();
    if (!conversation) return false;
    if (conversation.relationship === 'declined') return false;
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
