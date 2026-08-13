import { Component, computed, input, output, signal } from '@angular/core';
import { SearchBarComponent } from '../../discovery/search-bar/search-bar';
import { Conversation } from '../../../shared/models/chat.model';

@Component({
  selector: 'app-conversation-list',
  imports: [SearchBarComponent],
  templateUrl: './conversation-list.html',
  styleUrl: './conversation-list.css',
})
export class ConversationListComponent {
  conversations = input<Conversation[]>([]);
  selectedId = input<string | null>(null);

  select = output<Conversation>();

  showRequestsOnly = signal(false);

  requestsCount = computed(
    () => this.conversations().filter((c) => c.relationship === 'request-received').length,
  );

  visibleConversations = computed(() => {
    if (!this.showRequestsOnly()) return this.conversations();
    return this.conversations().filter((c) => c.relationship === 'request-received');
  });

  toggleRequestsFilter(): void {
    this.showRequestsOnly.update((value) => !value);
  }

  lastMessage(conversation: Conversation) {
    return conversation.messages[conversation.messages.length - 1];
  }
}
