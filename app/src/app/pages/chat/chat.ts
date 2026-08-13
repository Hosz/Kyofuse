import { Component, computed, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ConversationListComponent } from '../../components/chat/conversation-list/conversation-list';
import { ChatWindowComponent } from '../../components/chat/chat-window/chat-window';
import { Conversation } from '../../shared/models/chat.model';

const CONVERSATIONS: Conversation[] = [
  {
    id: 'v0id',
    participant: {
      name: 'Alexander "V0ID" Chen',
      handle: 'v0id_tactical',
      avatarUrl: 'https://i.pravatar.cc/160?img=15',
      badge: 'IGL',
      badgeTone: 'tertiary',
      online: true,
      statusLabel: '[PARTIDA: NA FILA]',
    },
    relationship: 'mutual',
    lastMessageAt: '12:44',
    unread: true,
    isTyping: true,
    messages: [
      {
        id: 'm1',
        author: 'them',
        content: 'Aí, vi o clipe que você postou no Kyofuse. Aquele triple spray de longe foi ridículo.',
        timestamp: '12:40',
      },
      {
        id: 'm2',
        author: 'them',
        content:
          'Precisamos de um substituto pras classificatórias regionais amanhã à noite. Você topa entrar? Tá faltando nosso entry fragger.',
        timestamp: '12:42',
      },
      {
        id: 'm3',
        author: 'me',
        content: 'Valeu! Tava sentindo o jogo mesmo. Amanhã à noite funciona pra mim, que horas é a primeira partida?',
        timestamp: '12:44',
        read: true,
      },
    ],
  },
  {
    id: 'ghost_panda',
    participant: {
      name: 'Ghost_Panda',
      handle: 'ghost_panda',
      avatarUrl: 'https://i.pravatar.cc/160?img=8',
      online: false,
    },
    relationship: 'mutual',
    lastMessageAt: '09:12',
    messages: [{ id: 'gp1', author: 'them', content: 'GG WP ontem!', timestamp: '09:12' }],
  },
  {
    id: 'strat_master',
    participant: {
      name: 'Strat_Master',
      handle: 'strat_master',
      avatarUrl: 'https://i.pravatar.cc/160?img=52',
      online: true,
    },
    relationship: 'mutual',
    lastMessageAt: 'Ontem',
    unread: true,
    messages: [{ id: 'sm1', author: 'them', content: 'Confere o PDF com a nova estratégia.', timestamp: 'Ontem' }],
  },
  {
    id: 'rival09',
    participant: {
      name: 'Rival_09',
      handle: 'rival09',
      avatarUrl: 'https://i.pravatar.cc/160?img=25',
      online: true,
    },
    relationship: 'request-received',
    lastMessageAt: '2h',
    unread: true,
    messages: [
      {
        id: 'r1',
        author: 'them',
        content: 'Fala! Vi que você tá procurando reserva pro time. Posso mandar minha demo?',
        timestamp: '2h',
      },
    ],
  },
  {
    id: 'zenithfrag',
    participant: {
      name: 'ZenithFrag',
      handle: 'zenithfrag',
      avatarUrl: 'https://i.pravatar.cc/160?img=21',
      online: false,
    },
    relationship: 'request-sent',
    lastMessageAt: '1d',
    messages: [],
  },
  {
    id: 'spambot',
    participant: {
      name: 'SpamBot_99',
      handle: 'spambot_99',
      avatarUrl: 'https://i.pravatar.cc/160?img=60',
      online: false,
    },
    relationship: 'declined',
    lastMessageAt: '3d',
    messages: [{ id: 'sb1', author: 'them', content: 'confira meu canal de boost barato!!', timestamp: '3d' }],
  },
];

@Component({
  selector: 'app-chat',
  imports: [AppSidebarComponent, ConversationListComponent, ChatWindowComponent],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class ChatComponent {
  private readonly router = inject(Router);

  /** Vinculado automaticamente ao parâmetro de rota :chatId (withComponentInputBinding). */
  chatId = input<string | null>(null);

  private readonly conversations = signal<Conversation[]>(CONVERSATIONS);

  selectedId = computed(() => this.chatId() ?? this.conversations()[0]?.id ?? null);

  allConversations = computed(() => this.conversations());

  selectedConversation = computed<Conversation | null>(
    () => this.conversations().find((conversation) => conversation.id === this.selectedId()) ?? null,
  );

  onSelectConversation(conversation: Conversation): void {
    this.router.navigate(['/chats', conversation.id]);
  }

  private updateConversation(id: string, updater: (conversation: Conversation) => Conversation): void {
    this.conversations.update((list) => list.map((conversation) => (conversation.id === id ? updater(conversation) : conversation)));
  }

  onAccept(): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;
    this.updateConversation(conversation.id, (c) => ({ ...c, relationship: 'mutual' }));
  }

  onDecline(): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;
    this.updateConversation(conversation.id, (c) => ({ ...c, relationship: 'declined' }));
  }

  onSendMessage(content: string): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;
    this.updateConversation(conversation.id, (c) => ({
      ...c,
      lastMessageAt: 'Agora',
      messages: [...c.messages, { id: `${c.id}-${c.messages.length + 1}`, author: 'me', content, timestamp: 'Agora' }],
    }));
  }
}
