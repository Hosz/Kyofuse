import { Component, computed, inject, input, output, signal } from '@angular/core';
import { SearchBarComponent } from '../../discovery/search-bar/search-bar';
import { Conversation } from '../../../shared/models/chat.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService } from '../../../core/i18n/i18n.service';

type ConversationFilter = 'all' | 'groups' | 'communities';

@Component({
  selector: 'app-conversation-list',
  imports: [SearchBarComponent, TranslatePipe],
  templateUrl: './conversation-list.html',
  styleUrl: './conversation-list.css',
})
export class ConversationListComponent {
  readonly i18n = inject(I18nService);

  conversations = input<Conversation[]>([]);
  selectedId = input<string | null>(null);

  select = output<Conversation>();
  newConversation = output<void>();

  showRequestsOnly = signal(false);
  activeFilter = signal<ConversationFilter>('all');

  readonly filters: { value: ConversationFilter; key: string }[] = [
    { value: 'all', key: 'chat.all' },
    { value: 'groups', key: 'chat.groups' },
    { value: 'communities', key: 'nav.communities' },
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

  previewLabel(conversation: Conversation): string {
    const preview = conversation.lastMessagePreview;
    if (!preview) {
      return this.i18n.t('chat.tapToChat');
    }
    if (preview === 'Quer trocar mensagens com você') return this.i18n.t('chat.requestReceivedPreview');
    if (preview === 'Solicitação enviada') return this.i18n.t('chat.requestSentPreview');
    if (preview === 'Conversa encerrada') return this.i18n.t('chat.declinedPreview');
    if (preview === 'Toque para conversar') return this.i18n.t('chat.tapToChat');
    if (preview === 'Canal da comunidade') return this.i18n.t('chat.channelCommunity');
    if (preview === 'Grupo de conversa') return this.i18n.t('chat.groupChat');

    const youWord = this.i18n.t('chat.you');
    const youRegex = /^(Você|You|Tú|Toi|Du|Вы|你|あなた):\s*/i;
    if (youRegex.test(preview)) {
      return preview.replace(youRegex, `${youWord}: `);
    }
    return preview;
  }
}
