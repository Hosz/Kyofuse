import { Component, computed, input, output, signal } from '@angular/core';
import { SearchBarComponent } from '../../discovery/search-bar/search-bar';
import { Conversation } from '../../../shared/models/chat.model';

type ConversationFilter = 'all' | 'groups' | 'communities';

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
  newConversation = output<void>();

  showRequestsOnly = signal(false);
  activeFilter = signal<ConversationFilter>('all');

  readonly filters: { value: ConversationFilter; label: string }[] = [
    { value: 'all', label: 'Geral' },
    { value: 'groups', label: 'Grupos' },
    { value: 'communities', label: 'Comunidades' },
  ];

  requestsCount = computed(
    () => this.conversations().filter((c) => c.relationship === 'request-received').length,
  );

  visibleConversations = computed(() => {
    // O filtro de solicitações é ortogonal às abas: quando ligado, mostra os pedidos
    // pendentes (que só existem em DIRECT) independentemente da aba selecionada.
    if (this.showRequestsOnly()) {
      return this.conversations().filter((c) => c.relationship === 'request-received');
    }

    const filter = this.activeFilter();
    if (filter === 'all') return this.conversations();
    if (filter === 'groups') return this.conversations().filter((c) => c.type === 'GROUP');
    return this.conversations().filter((c) => c.type === 'COMMUNITY');
  });

  setFilter(filter: ConversationFilter): void {
    this.activeFilter.set(filter);
    this.showRequestsOnly.set(false);
  }

  toggleRequestsFilter(): void {
    this.showRequestsOnly.update((value) => !value);
  }

  /**
   * ConversationResponse não traz um preview da última mensagem (nem a lista de
   * mensagens é carregada aqui, só ao abrir a conversa) — por isso o subtítulo é um
   * texto de status por tipo/relacionamento, não o conteúdo real da última mensagem.
   */
  previewLabel(conversation: Conversation): string {
    if (conversation.relationship === 'request-received') return 'Quer trocar mensagens com você';
    if (conversation.relationship === 'request-sent') return 'Solicitação enviada';
    if (conversation.relationship === 'declined') return 'Conversa encerrada';
    if (conversation.type === 'GROUP') return 'Grupo';
    if (conversation.type === 'COMMUNITY') return 'Comunidade';
    return 'Toque para abrir a conversa';
  }
}
