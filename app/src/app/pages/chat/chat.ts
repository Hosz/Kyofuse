import { Component, computed, effect, inject, input, OnDestroy, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ConversationListComponent } from '../../components/chat/conversation-list/conversation-list';
import { ChatWindowComponent } from '../../components/chat/chat-window/chat-window';
import { NewConversationModalComponent } from '../../components/chat/new-conversation-modal/new-conversation-modal';
import { Conversation, TypingEvent } from '../../shared/models/chat.model';
import { ConversationService } from '../../core/services/chat/conversation.service';
import { MessageService } from '../../core/services/chat/message.service';
import { PresenceService } from '../../core/services/presence/presence.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { ToastService } from '../../core/services/ui/toast.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ConversationResponse, MessageResponse } from '../../models/chat/chat.model';
import { PostMediaItemRequest } from '../../models/posts/post-request.model';
import { previewFromChatMessage, toChatMessage, toConversation } from '../../shared/utils/mappers.util';

const CONVERSATIONS_PAGE_SIZE = 50;
const MESSAGES_PAGE_SIZE = 50;

type ModalKind = 'new-conversation' | null;

@Component({
  selector: 'app-chat',
  imports: [AppSidebarComponent, ConversationListComponent, ChatWindowComponent, NewConversationModalComponent],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class ChatComponent implements OnDestroy {
  private readonly router = inject(Router);
  private readonly conversationService = inject(ConversationService);
  private readonly messageService = inject(MessageService);
  private readonly presenceService = inject(PresenceService);
  private readonly profileService = inject(ProfileService);
  private readonly toastService = inject(ToastService);
  private readonly i18n = inject(I18nService);

  /** Vinculado automaticamente ao parâmetro de rota :chatId (withComponentInputBinding). */
  chatId = input<string | null>(null);

  private readonly myUserId = signal<string | null>(null);
  private readonly conversations = signal<Conversation[]>([]);
  private readonly loadedMessagesFor = new Set<string>();

  private chatSub?: Subscription;
  private statusSub?: Subscription;
  private userQueueSub?: Subscription;
  private readonly typingSubs = new Map<string, Subscription>();
  private readonly typingTrackers = new Map<string, Map<string, { name: string; timeoutId: any }>>();

  readonly allConversations = computed(() => {
    const list = this.conversations();
    const presenceMap = this.presenceService.presenceMap();
    return list.map((conv) => {
      if (conv.type === 'DIRECT' && conv.participant.id) {
        const presence = presenceMap.get(conv.participant.id);
        const online = presence?.status === 'ONLINE';
        return {
          ...conv,
          participant: {
            ...conv.participant,
            online,
          },
        };
      }
      return conv;
    });
  });
  readonly selectedId = computed(() => this.chatId() ?? null);
  readonly selectedConversation = computed(
    () => this.allConversations().find((c) => c.id === this.selectedId()) ?? null,
  );
  readonly myUserIdValue = this.myUserId.asReadonly();

  activeModal = signal<ModalKind>(null);
  loadingOlderMessages = signal(false);

  constructor() {
    this.conversationService.markAsRead();
    this.profileService.myProfile().subscribe({
      next: (profile) => {
        this.myUserId.set(profile.userId);
        this.loadConversations();
      },
      error: (error) => console.error('Failed to fetch my profile:', error),
    });

    this.statusSub = this.messageService.messageStatus$.subscribe({
      next: (event) => {
        this.updateConversation(event.conversationId, (c) => ({
          ...c,
          messages: c.messages.map((m) => (m.id === event.messageId ? { ...m, status: event.status } : m)),
        }));
      },
      error: (err) => console.error('[ChatComponent] Realtime status error:', err),
    });

    this.userQueueSub = this.conversationService.newMessage$.subscribe({
      next: (messageResponse) => {
        const myId = this.myUserId();
        if (!myId) return;
        this.handleIncomingMessage(messageResponse, myId);
      },
      error: (err) => console.error('[ChatComponent] User queue error:', err),
    });

    // Roda de novo sempre que a conversa selecionada mudar (troca de rota ou seleção
    // na lista) — carrega o histórico de mensagens e conecta ao WebSocket da conversa.
    effect(() => {
      const conversationId = this.selectedId();
      const myId = this.myUserId();
      if (conversationId && myId) {
        untracked(() => {
          this.ensureMessagesLoaded(conversationId, myId);
          this.subscribeToRealtimeMessages(conversationId, myId);
        });
      } else {
        untracked(() => {
          this.chatSub?.unsubscribe();
          this.chatSub = undefined;
        });
      }
    });
  }

  onSelectConversation(conversation: Conversation): void {
    this.router.navigate(['/chats', conversation.id]);
  }

  onAccept(): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;
    this.conversationService.acceptDirectConversation(conversation.id).subscribe({
      next: (response) => this.replaceConversation(response),
      error: (error) => console.error('Failed to accept conversation:', error),
    });
  }

  onDecline(): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;
    this.conversationService.declineDirectConversation(conversation.id).subscribe({
      next: (response) => this.replaceConversation(response),
      error: (error) => console.error('Failed to decline conversation:', error),
    });
  }

  onRevoke(): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;
    this.conversationService.revokeDirectConversationPermission(conversation.id).subscribe({
      next: (response) => this.replaceConversation(response),
      error: (error) => console.error('Failed to revoke conversation permission:', error),
    });
  }

  onAllow(): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;
    this.conversationService.allowDirectConversationPermission(conversation.id).subscribe({
      next: (response) => {
        this.replaceConversation(response);
        this.toastService.success('Conversa liberada com sucesso.');
      },
      error: (error) => {
        console.error('Failed to allow conversation permission:', error);
        this.toastService.error('Não foi possível liberar a conversa.');
      },
    });
  }

  onSendMessage(payload: { content?: string; media?: PostMediaItemRequest[] }): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;

    // Respondeu à conversa: limpa a marcação de novas mensagens imediatamente
    this.updateConversation(conversation.id, (c) => ({
      ...c,
      unreadDividerMessageId: null,
    }));

    this.messageService.sendMessage(conversation.id, payload).subscribe({
      next: (response) => {
        const myId = this.myUserId();
        if (!myId) return;
        this.handleIncomingMessage(response, myId);
      },
      error: (error) => {
        console.error('Failed to send message:', error);
        this.toastService.error('Não foi possível enviar a mensagem.');
      },
    });
  }

  onDeleteMessage(messageId: string): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;

    this.messageService.deleteMessage(conversation.id, messageId).subscribe({
      next: () =>
        this.updateConversation(conversation.id, (c) => ({
          ...c,
          messages: c.messages.filter((message) => message.id !== messageId),
        })),
      error: (error) => {
        console.error('Failed to delete message:', error);
        this.toastService.error('Não foi possível apagar a mensagem.');
      },
    });
  }

  onGroupUpdated(response: ConversationResponse): void {
    this.replaceConversation(response);
  }

  openNewConversationModal(): void {
    this.activeModal.set('new-conversation');
  }

  closeModal(): void {
    this.activeModal.set(null);
  }

  onStartDirectConversation(participantId: string): void {
    this.conversationService.createConversation({ participantIds: [participantId] }).subscribe({
      next: (response) => {
        this.closeModal();
        this.upsertConversation(response);
        this.router.navigate(['/chats', response.id]);
      },
      error: (error) => console.error('Failed to start conversation:', error),
    });
  }

  onCreateGroupConversation(payload: { name: string; avatarUrl?: string; participantIds: string[] }): void {
    this.conversationService.createConversation(payload).subscribe({
      next: (response) => {
        this.closeModal();
        this.upsertConversation(response);
        this.router.navigate(['/chats', response.id]);
      },
      error: (error) => console.error('Failed to create group:', error),
    });
  }

  private loadConversations(): void {
    forkJoin({
      direct: this.conversationService.listDirectConversations(0, CONVERSATIONS_PAGE_SIZE),
      group: this.conversationService.listGroupConversations(0, CONVERSATIONS_PAGE_SIZE),
      community: this.conversationService.listCommunityConversations(0, CONVERSATIONS_PAGE_SIZE),
    }).subscribe({
      next: ({ direct, group, community }) => {
        const responses = [...direct.content, ...group.content, ...community.content].sort(
          (a, b) => {
            const timeA = new Date(a.lastMessageCreatedAt ?? a.updatedAt).getTime();
            const timeB = new Date(b.lastMessageCreatedAt ?? b.updatedAt).getTime();
            return timeB - timeA;
          },
        );
        const existingMap = new Map(this.conversations().map((c) => [c.id, c]));
        this.conversations.set(
          responses.map((response) => {
            const uiConv = this.toUiConversation(response);
            const existing = existingMap.get(response.id);
            return existing
              ? {
                  ...uiConv,
                  messages: existing.messages.length > 0 ? existing.messages : uiConv.messages,
                  isTyping: existing.isTyping,
                  typingText: existing.typingText,
                }
              : uiConv;
          }),
        );

        const selectedId = this.selectedId();
        const myId = this.myUserId();

        if (myId) {
          this.syncTypingSubscriptions(responses.map((r) => r.id), myId);
        }

        // Busca o status de presença de todos os participantes das conversas diretas
        const directUserIds = direct.content
          .flatMap((c) => [c.directUserOneId, c.directUserTwoId])
          .filter((id): id is string => !!id && id !== myId);
        if (directUserIds.length > 0) {
          this.presenceService.fetchBatchPresence(directUserIds);
        }

        if (selectedId && myId) {
          const current = this.conversations().find((c) => c.id === selectedId);
          if (current && current.messages.length === 0) {
            this.loadedMessagesFor.delete(selectedId);
            this.ensureMessagesLoaded(selectedId, myId);
          }
        }
      },
      error: (error) => console.error('Failed to fetch conversations:', error),
    });
  }

  private ensureMessagesLoaded(conversationId: string, myId: string): void {
    const target = this.conversations().find((c) => c.id === conversationId);
    if (!target && this.conversations().length > 0) {
      this.conversationService.getConversationDetails(conversationId).subscribe({
        next: (response) => {
          this.upsertConversation(response);
          this.fetchMessages(conversationId, myId, 0);
        },
        error: (err) => console.error('Failed to get conversation details:', err),
      });
      return;
    }

    const unreadToClear = target?.unreadCount ?? 0;
    if (unreadToClear > 0) {
      this.conversationService.decrementUnread(unreadToClear);
    }

    let unreadDividerMessageId: string | null = null;
    if (unreadToClear > 0 && target && target.messages.length > 0) {
      const firstUnreadIndex = Math.max(0, target.messages.length - unreadToClear);
      unreadDividerMessageId = target.messages[firstUnreadIndex]?.id ?? null;
    }

    if (target && (target.unread || (target.unreadCount ?? 0) > 0 || target.unreadDividerMessageId)) {
      this.updateConversation(conversationId, (c) => ({
        ...c,
        unread: false,
        unreadCount: 0,
        unreadDividerMessageId: unreadToClear > 0 ? (unreadDividerMessageId ?? c.unreadDividerMessageId) : null,
      }));
    }

    this.fetchMessages(conversationId, myId, unreadToClear);
  }

  onLoadOlderMessages(): void {
    const conversation = this.selectedConversation();
    const myId = this.myUserId();
    if (!conversation || !myId || this.loadingOlderMessages() || !conversation.hasMoreMessages) {
      return;
    }

    const nextPage = (conversation.messagesPage ?? 0) + 1;
    this.loadingOlderMessages.set(true);

    this.messageService.getMessages(conversation.id, nextPage, MESSAGES_PAGE_SIZE).subscribe({
      next: (response) => {
        const olderMessages = response.content
          .slice()
          .reverse()
          .map((message) => toChatMessage(message, myId));

        this.updateConversation(conversation.id, (c) => {
          const existingIds = new Set(c.messages.map((m) => m.id));
          const filteredOlder = olderMessages.filter((m) => !existingIds.has(m.id));
          return {
            ...c,
            messages: [...filteredOlder, ...c.messages],
            hasMoreMessages: !response.last,
            messagesPage: nextPage,
          };
        });
        this.loadingOlderMessages.set(false);
      },
      error: (error) => {
        console.error('Failed to load older messages:', error);
        this.loadingOlderMessages.set(false);
      },
    });
  }

  private fetchMessages(conversationId: string, myId: string, unreadCountBeforeClear: number = 0): void {
    if (this.loadedMessagesFor.has(conversationId)) {
      this.messageService.markAsRead(conversationId).subscribe({
        error: (err) => console.error('Failed to mark conversation as read:', err),
      });
      return;
    }
    this.loadedMessagesFor.add(conversationId);

    this.messageService.getMessages(conversationId, 0, MESSAGES_PAGE_SIZE).subscribe({
      next: (response) => {
        const messages = response.content
          .slice()
          .reverse()
          .map((message) => toChatMessage(message, myId));

        let unreadDividerMessageId: string | null = null;
        if (unreadCountBeforeClear > 0 && messages.length > 0) {
          const firstUnreadIndex = Math.max(0, messages.length - unreadCountBeforeClear);
          unreadDividerMessageId = messages[firstUnreadIndex]?.id ?? null;
        }

        this.updateConversation(conversationId, (c) => ({
          ...c,
          messages,
          unread: false,
          unreadCount: 0,
          hasMoreMessages: !response.last,
          messagesPage: 0,
          unreadDividerMessageId: unreadDividerMessageId ?? c.unreadDividerMessageId ?? null,
        }));
        this.messageService.markAsRead(conversationId).subscribe({
          error: (err) => console.error('Failed to mark conversation as read:', err),
        });
      },
      error: (error) => {
        console.error('Failed to fetch messages:', error);
        this.loadedMessagesFor.delete(conversationId);
      },
    });
  }

  private handleIncomingMessage(messageResponse: MessageResponse, myId: string): void {
    const chatMsg = toChatMessage(messageResponse, myId);
    const conversationId = messageResponse.conversationId;
    const isCurrent = this.selectedId() === conversationId;

    this.conversations.update((list) => {
      const targetIndex = list.findIndex((c) => c.id === conversationId);
      if (targetIndex === -1) return list;

      const target = list[targetIndex];
      const exists = target.messages.some((m) => m.id === chatMsg.id);
      const newMessages = exists ? target.messages : [...target.messages, chatMsg];
      const preview = previewFromChatMessage(chatMsg, target.type !== 'DIRECT');

      const isThem = messageResponse.senderId !== myId;
      const unreadCount = isCurrent || !isThem ? 0 : (target.unreadCount ?? 0) + (exists ? 0 : 1);

      const updated: Conversation = {
        ...target,
        lastMessageAt: 'Agora',
        lastMessagePreview: preview,
        unread: unreadCount > 0 || (target.relationship === 'request-received' && !isCurrent),
        unreadCount,
        messages: newMessages,
      };

      const filtered = list.filter((c) => c.id !== conversationId);
      return [updated, ...filtered];
    });

    if (messageResponse.senderId !== myId) {
      const userMap = this.typingTrackers.get(conversationId);
      if (userMap && userMap.has(messageResponse.senderId)) {
        const tracker = userMap.get(messageResponse.senderId);
        if (tracker) {
          clearTimeout(tracker.timeoutId);
        }
        userMap.delete(messageResponse.senderId);
        this.applyTypingState(conversationId);
      }

      this.messageService.markAsDelivered([messageResponse.id]).subscribe();
      if (isCurrent) {
        this.messageService.markAsRead(conversationId).subscribe();
        this.conversationService.decrementUnread(1);
      }
    }
  }

  /** Preserva as mensagens e o estado de digitação já carregados — a resposta desses endpoints é só o
   * envelope da conversa, não vem com o histórico de mensagens junto. */
  private replaceConversation(response: ConversationResponse): void {
    const mapped = this.toUiConversation(response);
    this.updateConversation(response.id, (c) => ({
      ...mapped,
      messages: c.messages,
      isTyping: c.isTyping,
      typingText: c.typingText,
    }));
  }

  private upsertConversation(response: ConversationResponse): void {
    const mapped = this.toUiConversation(response);
    this.conversations.update((list) => {
      const exists = list.some((c) => c.id === mapped.id);
      return exists
        ? list.map((c) => (c.id === mapped.id ? { ...mapped, messages: c.messages, isTyping: c.isTyping, typingText: c.typingText } : c))
        : [mapped, ...list];
    });
    const myId = this.myUserId();
    if (myId) {
      this.syncTypingSubscriptions([response.id], myId);
    }
  }

  private toUiConversation(response: ConversationResponse): Conversation {
    return toConversation(response, this.myUserId() ?? '');
  }

  private subscribeToRealtimeMessages(conversationId: string, myId: string): void {
    this.chatSub?.unsubscribe();
    this.chatSub = this.messageService.watchConversation(conversationId).subscribe({
      next: (messageResponse) => {
        this.handleIncomingMessage(messageResponse, myId);
      },
      error: (err) => console.error('[ChatComponent] Realtime WebSocket error:', err),
    });
  }

  private syncTypingSubscriptions(conversationIds: string[], myId: string): void {
    for (const id of conversationIds) {
      if (!this.typingSubs.has(id)) {
        const sub = this.messageService.watchTyping(id).subscribe({
          next: (event) => this.handleTypingEvent(event, myId),
          error: (err) => console.debug('[ChatComponent] Typing error:', err),
        });
        this.typingSubs.set(id, sub);
      }
    }
  }

  private handleTypingEvent(event: TypingEvent, myId: string): void {
    if (event.userId === myId) return;

    const convId = event.conversationId;
    let userMap = this.typingTrackers.get(convId);
    if (!userMap) {
      userMap = new Map();
      this.typingTrackers.set(convId, userMap);
    }

    const existing = userMap.get(event.userId);
    if (existing) {
      clearTimeout(existing.timeoutId);
      userMap.delete(event.userId);
    }

    if (event.isTyping) {
      const name = event.nickname?.trim() || event.username || 'Alguém';
      const timeoutId = setTimeout(() => {
        const currentMap = this.typingTrackers.get(convId);
        if (currentMap) {
          currentMap.delete(event.userId);
          this.applyTypingState(convId);
        }
      }, 3500);

      userMap.set(event.userId, { name, timeoutId });
    }

    this.applyTypingState(convId);
  }

  private applyTypingState(convId: string): void {
    const userMap = this.typingTrackers.get(convId);
    const users = userMap ? Array.from(userMap.values()) : [];
    const isTyping = users.length > 0;

    this.updateConversation(convId, (conv) => {
      let typingText: string | undefined = undefined;
      if (isTyping) {
        if (conv.type === 'DIRECT') {
          typingText = this.i18n.t('chat.isTyping');
        } else {
          if (users.length === 1) {
            typingText = this.i18n.t('chat.typingSingular', { name: users[0].name });
          } else if (users.length === 2) {
            typingText = this.i18n.t('chat.typingDual', { name1: users[0].name, name2: users[1].name });
          } else {
            typingText = this.i18n.t('chat.typingMultiple', { count: users.length });
          }
        }
      }
      return {
        ...conv,
        isTyping,
        typingText,
      };
    });
  }

  ngOnDestroy(): void {
    this.chatSub?.unsubscribe();
    this.statusSub?.unsubscribe();
    this.userQueueSub?.unsubscribe();

    for (const sub of this.typingSubs.values()) {
      sub.unsubscribe();
    }
    this.typingSubs.clear();

    for (const userMap of this.typingTrackers.values()) {
      for (const tracker of userMap.values()) {
        clearTimeout(tracker.timeoutId);
      }
      userMap.clear();
    }
    this.typingTrackers.clear();
  }

  onBackToConversations(): void {
    this.router.navigate(['/chats']);
  }

  private updateConversation(id: string, updater: (conversation: Conversation) => Conversation): void {
    this.conversations.update((list) => list.map((conversation) => (conversation.id === id ? updater(conversation) : conversation)));
  }
}
