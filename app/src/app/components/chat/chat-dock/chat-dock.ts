import { Component, ElementRef, afterRenderEffect, computed, inject, OnDestroy, signal, viewChild } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, forkJoin, map, startWith, Subscription } from 'rxjs';
import { ConversationService } from '../../../core/services/chat/conversation.service';
import { MessageService } from '../../../core/services/chat/message.service';
import { ProfileService } from '../../../core/services/profile/profile.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { ConversationInfoPanelComponent } from '../conversation-info-panel/conversation-info-panel';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';
import { SkeletonComponent } from '../../shared/skeleton/skeleton';
import { FullMessageModalComponent } from '../full-message-modal/full-message-modal';
import { FullMessageViewData } from '../full-message-modal/full-message-modal.types';
import { ToastService } from '../../../core/services/ui/toast.service';
import { ConversationResponse, MessageResponse } from '../../../models/chat/chat.model';
import { Conversation, TypingEvent, ChatMessage, ChatMessageGroup } from '../../../shared/models/chat.model';
import { previewFromChatMessage, toChatMessage, toChatMessageGroups, toConversation } from '../../../shared/utils/mappers.util';
import { toTimeAgo, isLongChatMessage, truncateChatMessage, FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService } from '../../../core/i18n/i18n.service';

const CONVERSATIONS_PAGE_SIZE = 30;
const MESSAGES_PAGE_SIZE = 30;

type DockFilter = 'all' | 'groups' | 'communities';

/** Ignora acento e caixa pra busca não depender de o usuário digitar "comunidade
 * tática" com o acento certo. */
function normalize(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
}

/**
 * Chat portátil, no espírito do Instagram e do X: fica no canto inferior direito de
 * qualquer página, expande numa janelinha e lê/responde conversas ali mesmo, sem sair
 * do que o usuário estava fazendo. Some dentro de /chats, onde a página inteira já é
 * isso em tamanho grande.
 */
@Component({
  selector: 'app-chat-dock',
  imports: [ConversationInfoPanelComponent, ConfirmDialogComponent, SkeletonComponent, FullMessageModalComponent, TranslatePipe],
  templateUrl: './chat-dock.html',
  styleUrl: './chat-dock.css',
})
export class ChatDockComponent implements OnDestroy {
  readonly i18n = inject(I18nService);
  private router = inject(Router);
  private conversationService = inject(ConversationService);
  private messageService = inject(MessageService);
  private profileService = inject(ProfileService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  private scroller = viewChild<ElementRef<HTMLElement>>('scroller');
  private chatSub: Subscription | null = null;
  private userQueueSub: Subscription | null = null;
  private statusSub: Subscription | null = null;
  private conversationCreatedSub: Subscription | null = null;
  private readonly typingSubs = new Map<string, Subscription>();
  private readonly typingTrackers = new Map<string, Map<string, { name: string; timeoutId: any }>>();

  private currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url },
  );

  /** Escondido dentro de /chats (redundante) e fora de sessão — na tela de acesso não
   * há usuário pra listar conversas. */
  hidden = computed(() => this.currentUrl().startsWith('/chats') || !this.authService.isAuthenticated());

  open = signal(false);
  loading = signal(false);
  conversations = signal<Conversation[]>([]);

  activeFilter = signal<DockFilter>('all');
  query = signal('');
  infoPanelOpen = signal(false);

  readonly filters: { value: DockFilter; labelKey: string }[] = [
    { value: 'all', labelKey: 'chat.all' },
    { value: 'groups', labelKey: 'chat.groups' },
    { value: 'communities', labelKey: 'chat.community' },
  ];

  /** Conversa aberta dentro da janelinha. null = mostrando a lista. */
  activeId = signal<string | null>(null);
  messagesLoading = signal(false);
  loadingOlderMessages = signal(false);
  sending = signal(false);
  draft = signal('');

  messagePendingDeletion = signal<string | null>(null);
  selectedFullMessage = signal<FullMessageViewData | null>(null);

  private myUserId = signal<string | null>(null);
  private loaded = false;
  private loadedMessagesFor = new Set<string>();
  private previousScrollHeight: number | null = null;
  private previousScrollTop: number | null = null;
  private lastMessageCount = 0;
  private currentConvId: string | null = null;

  /** Aba e busca se somam: a busca procura dentro do que a aba já deixou passar. */
  visibleConversations = computed(() => {
    const filter = this.activeFilter();
    const byTab =
      filter === 'all'
        ? this.conversations()
        : this.conversations().filter((c) => c.type === (filter === 'groups' ? 'GROUP' : 'COMMUNITY'));

    const term = normalize(this.query().trim());
    if (!term) return byTab;

    return byTab.filter(
      (c) => normalize(c.participant.name).includes(term) || normalize(c.participant.handle).includes(term),
    );
  });

  /** O painel de membros/perfil precisa saber quem sou eu pra decidir as ações de admin. */
  myUserIdValue = computed(() => this.myUserId());

  active = computed(() => this.conversations().find((conversation) => conversation.id === this.activeId()) ?? null);

  messageGroups = computed(() =>
    toChatMessageGroups(
      this.active()?.messages ?? [],
      this.active()?.unreadDividerMessageId,
      this.i18n.currentLang(),
      this.i18n.t('chat.newMessages'),
    ),
  );

  /** Mesma regra da janela grande: quem recebeu um pedido ainda não aceito não pode
   * responder, e quem enviou só tem direito a uma mensagem até ser aceito. */
  canType = computed(() => {
    const conversation = this.active();
    if (!conversation) return false;
    if (conversation.relationship === 'declined' || conversation.relationship === 'request-received') return false;
    if (conversation.relationship === 'request-sent') return !conversation.messages.some((m) => m.author === 'me');
    return true;
  });

  /** O aceite/recusa de uma solicitação mora na página cheia — aqui só avisamos. */
  blockedNotice = computed(() => {
    const conversation = this.active();
    if (!conversation || this.canType()) return null;
    switch (conversation.relationship) {
      case 'request-received':
        return this.i18n.t('chat.blockedRequestReceived');
      case 'request-sent':
        return this.i18n.t('chat.blockedRequestSent');
      case 'declined': {
        const myId = this.myUserId();
        const canAllow = !conversation.revokedById || conversation.revokedById === myId;
        return canAllow
          ? this.i18n.t('chat.blockedEndedByYou')
          : this.i18n.t('chat.blockedEndedByOther').replace('{name}', conversation.participant.name);
      }
      default:
        return this.i18n.t('chat.blockedUnavailable');
    }
  });

  constructor() {
    this.statusSub = this.messageService.messageStatus$.subscribe({
      next: (event) => {
        this.updateConversation(event.conversationId, (c) => ({
          ...c,
          messages: c.messages.map((m) => (m.id === event.messageId ? { ...m, status: event.status } : m)),
        }));
      },
      error: (err) => console.error('[ChatDock] Realtime status error:', err),
    });

    this.userQueueSub = this.conversationService.newMessage$.subscribe({
      next: (messageResponse) => {
        const myId = this.myUserId();
        if (!myId) return;
        this.handleIncomingMessage(messageResponse, myId);
      },
      error: (err) => console.error('[ChatDock] User queue error:', err),
    });

    this.profileService.myProfile().subscribe({
      next: (profile) => this.myUserId.set(profile.userId),
      error: () => {},
    });

    this.conversationCreatedSub = this.conversationService.conversationCreated$.subscribe({
      next: (conversation) => {
        const myId = this.myUserId();
        if (myId) {
          const mapped = toConversation(conversation, myId, this.i18n.currentLang());
          this.conversations.update((list) => {
            const filtered = list.filter((c) => c.id !== mapped.id);
            return [mapped, ...filtered];
          });
          this.syncTypingSubscriptions([mapped.id], myId);
        } else {
          this.load();
        }
      },
    });

    // Rola pro fim sempre que a conversa aberta muda ou recebe mensagem nova. Precisa
    // rodar depois do render: a altura só é a final quando as mensagens já estão no DOM.
    afterRenderEffect({
      write: () => {
        this.messageGroups();
        const conv = this.active();
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
          const unreadDividerEl = element.querySelector('#dock-unread-divider') as HTMLElement | null;
          if (unreadDividerEl) {
            unreadDividerEl.scrollIntoView({ block: 'start', behavior: 'instant' });
          } else {
            element.scrollTop = element.scrollHeight;
          }
        } else if (hasNewMessages) {
          const isMe = conv?.messages.at(-1)?.author === 'me';
          const isNearBottom = element.scrollHeight - element.scrollTop - element.clientHeight < 120;
          if (isMe || isNearBottom) {
            element.scrollTo({
              top: element.scrollHeight,
              behavior: 'smooth',
            });
          }
        }
      },
    });
  }

  setFilter(filter: DockFilter): void {
    this.activeFilter.set(filter);
  }

  onQueryChange(value: string): void {
    this.query.set(value);
  }

  previewLabel(conversation: Conversation): string {
    const preview = conversation.lastMessagePreview;
    if (!preview) {
      if (conversation.type === 'DIRECT') return '@' + conversation.participant.handle;
      return conversation.type === 'GROUP' ? this.i18n.t('chat.group') : this.i18n.t('chat.community');
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

  openInfoPanel(): void {
    this.infoPanelOpen.set(true);
  }

  closeInfoPanel(): void {
    this.infoPanelOpen.set(false);
  }

  /** O painel só devolve o envelope da conversa; as mensagens já carregadas ficam. */
  onGroupUpdated(response: ConversationResponse): void {
    const myId = this.myUserId();
    if (!myId) return;
    const mapped = toConversation(response, myId);
    this.updateConversation(response.id, (c) => ({ ...mapped, messages: c.messages }));
  }

  readonly totalUnreadCount = this.conversationService.unreadCount;
  readonly hasUnread = this.conversationService.hasUnread;

  toggle(): void {
    const next = !this.open();
    this.open.set(next);
    if (next) {
      this.loaded = true;
      this.load();
    }
  }

  close(): void {
    this.chatSub?.unsubscribe();
    this.open.set(false);
  }

  openConversation(conversation: Conversation): void {
    const unreadToClear = conversation.unreadCount ?? 0;
    if (unreadToClear > 0) {
      this.conversationService.decrementUnread(unreadToClear);
    }
    this.activeId.set(conversation.id);
    this.draft.set('');

    let unreadDividerMessageId: string | null = null;
    if (unreadToClear > 0 && conversation.messages.length > 0) {
      const firstUnreadIndex = Math.max(0, conversation.messages.length - unreadToClear);
      unreadDividerMessageId = conversation.messages[firstUnreadIndex]?.id ?? null;
    }

    this.updateConversation(conversation.id, (c) => ({
      ...c,
      unread: false,
      unreadCount: 0,
      unreadDividerMessageId: unreadToClear > 0 ? (unreadDividerMessageId ?? c.unreadDividerMessageId) : null,
    }));
    const myId = this.myUserId();
    if (myId) {
      this.ensureMessagesLoaded(conversation.id, myId, unreadToClear);
      this.subscribeToRealtimeMessages(conversation.id, myId);
      this.messageService.markAsRead(conversation.id).subscribe();
    }
  }

  back(): void {
    this.chatSub?.unsubscribe();
    this.activeId.set(null);
    this.infoPanelOpen.set(false);
  }

  /** Leva o que está aberto na janelinha pra página cheia. */
  expand(): void {
    const conversationId = this.activeId();
    this.close();
    this.router.navigate(conversationId ? ['/chats', conversationId] : ['/chats']);
  }

  onDraftChange(value: string): void {
    this.draft.set(value);
  }

  send(): void {
    const conversation = this.active();
    const content = this.draft().trim();
    const myId = this.myUserId();
    if (!conversation || !content || !myId || !this.canType() || this.sending()) return;

    if (content.length > 12000) {
      this.toastService.error('A mensagem não pode exceder 12.000 caracteres.');
      return;
    }

    // Respondeu à conversa: limpa a marcação de novas mensagens imediatamente
    this.updateConversation(conversation.id, (c) => ({
      ...c,
      unreadDividerMessageId: null,
    }));

    this.sending.set(true);

    this.messageService.sendMessage(conversation.id, { content }).subscribe({
      next: (response) => {
        this.draft.set('');
        this.sending.set(false);
        this.handleIncomingMessage(response, myId);
      },
      error: (error) => {
        console.error('Failed to send message:', error);
        this.sending.set(false);
        this.toastService.error('Não foi possível enviar a mensagem.');
      },
    });
  }

  isLong(text: string | null | undefined): boolean {
    return isLongChatMessage(text);
  }

  truncate(text: string | null | undefined): string {
    return truncateChatMessage(text);
  }

  openFullMessage(message: ChatMessage, group?: ChatMessageGroup): void {
    const conv = this.active();
    const isMe = message.author === 'me';
    const senderName = isMe
      ? this.i18n.t('chat.you')
      : (message.senderNickname || group?.senderNickname || conv?.participant.name || 'Usuário');
    const senderHandle = isMe
      ? undefined
      : (message.senderUsername || group?.senderUsername || conv?.participant.handle || undefined);
    const senderAvatarUrl = isMe
      ? undefined
      : (message.senderAvatarUrl || group?.senderAvatarUrl || conv?.participant.avatarUrl || FALLBACK_AVATAR_URL);

    this.selectedFullMessage.set({
      id: message.id,
      content: message.content,
      author: message.author,
      senderName,
      senderHandle,
      senderAvatarUrl,
      timestamp: message.exactTime || group?.timeFormatted || message.timestamp,
      tooltipTime: message.tooltipTime,
      media: message.media,
    });
  }

  closeFullMessage(): void {
    this.selectedFullMessage.set(null);
  }

  askDeleteMessage(messageId: string): void {
    this.messagePendingDeletion.set(messageId);
  }

  cancelDeleteMessage(): void {
    this.messagePendingDeletion.set(null);
  }

  confirmDeleteMessage(): void {
    const messageId = this.messagePendingDeletion();
    const conversation = this.active();
    this.messagePendingDeletion.set(null);
    if (!messageId || !conversation) return;

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

  private handleIncomingMessage(messageResponse: MessageResponse, myId: string): void {
    const chatMsg = toChatMessage(messageResponse, myId);
    const conversationId = messageResponse.conversationId;
    const isCurrent = this.open() && this.activeId() === conversationId;

    this.conversations.update((list) => {
      const targetIndex = list.findIndex((c) => c.id === conversationId);
      if (targetIndex === -1) {
        this.conversationService.getConversationDetails(conversationId).subscribe({
          next: (conv) => {
            const mapped = toConversation(conv, myId, this.i18n.currentLang());
            const isThem = messageResponse.senderId !== myId;
            const unreadCount = isCurrent || !isThem ? 0 : 1;
            const newConv: Conversation = {
              ...mapped,
              lastMessageAt: toTimeAgo(new Date().toISOString(), this.i18n.currentLang()),
              lastMessagePreview: previewFromChatMessage(chatMsg, mapped.type !== 'DIRECT'),
              unread: unreadCount > 0 || (mapped.relationship === 'request-received' && !isCurrent),
              unreadCount,
              messages: [chatMsg],
            };
            this.conversations.update((current) => [newConv, ...current.filter((c) => c.id !== conv.id)]);
            this.syncTypingSubscriptions([conv.id], myId);
          },
          error: (err) => console.error('[ChatDock] Failed to fetch incoming conversation:', err),
        });
        return list;
      }

      const target = list[targetIndex];
      const exists = target.messages.some((m) => m.id === chatMsg.id);
      const newMessages = exists ? target.messages : [...target.messages, chatMsg];
      const preview = previewFromChatMessage(chatMsg, target.type !== 'DIRECT');

      const isThem = messageResponse.senderId !== myId;
      const unreadCount = isCurrent || !isThem ? 0 : (target.unreadCount ?? 0) + (exists ? 0 : 1);

      const updated: Conversation = {
        ...target,
        lastMessageAt: toTimeAgo(new Date().toISOString(), this.i18n.currentLang()),
        lastMessagePreview: preview,
        unread: unreadCount > 0 || (target.relationship === 'request-received' && !isCurrent),
        unreadCount,
        messages: newMessages,
      };

      const filtered = list.filter((c) => c.id !== conversationId);
      return [updated, ...filtered];
    });

    this.syncTypingSubscriptions([conversationId], myId);

    if (messageResponse.senderId !== myId) {
      this.messageService.markAsDelivered([messageResponse.id]).subscribe();
      if (isCurrent) {
        this.messageService.markAsRead(conversationId).subscribe();
        this.conversationService.decrementUnread(1);
      }
    }
  }

  onScroll(): void {
    const element = this.scroller()?.nativeElement;
    const conversation = this.active();
    if (!element || !conversation) return;

    if (element.scrollTop < 60 && conversation.hasMoreMessages && !this.loadingOlderMessages() && !this.messagesLoading()) {
      this.previousScrollHeight = element.scrollHeight;
      this.previousScrollTop = element.scrollTop;
      this.loadOlderMessages();
    }
  }

  loadOlderMessages(): void {
    const conversation = this.active();
    const myId = this.myUserId();
    if (!conversation || !myId || this.loadingOlderMessages() || !conversation.hasMoreMessages) return;

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
        console.error('Failed to load older messages in dock:', error);
        this.loadingOlderMessages.set(false);
      },
    });
  }

  private ensureMessagesLoaded(conversationId: string, myId: string, unreadCountBeforeClear: number = 0): void {
    if (this.loadedMessagesFor.has(conversationId)) return;
    this.loadedMessagesFor.add(conversationId);
    this.messagesLoading.set(true);

    this.messageService.getMessages(conversationId, 0, MESSAGES_PAGE_SIZE).subscribe({
      next: (response) => {
        // O endpoint devolve as mais recentes primeiro; a exibição é cronológica.
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
        this.messagesLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch messages:', error);
        this.loadedMessagesFor.delete(conversationId);
        this.messagesLoading.set(false);
      },
    });
  }

  private load(): void {
    if (this.conversations().length === 0) {
      this.loading.set(true);
    }

    this.profileService.myProfile().subscribe({
      next: (profile) => {
        this.myUserId.set(profile.userId);

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
            const mapped = responses.map((response) => toConversation(response, profile.userId, this.i18n.currentLang()));
            this.conversations.set(mapped);
            this.syncTypingSubscriptions(mapped.map((c) => c.id), profile.userId);
            this.loading.set(false);
          },
          error: (error) => {
            console.error('Failed to fetch conversations:', error);
            this.loading.set(false);
          },
        });
      },
      error: (error) => {
        console.error('Failed to fetch my profile:', error);
        this.loading.set(false);
      },
    });
  }

  private subscribeToRealtimeMessages(conversationId: string, myId: string): void {
    this.chatSub?.unsubscribe();
    this.chatSub = this.messageService.watchConversation(conversationId).subscribe({
      next: (messageResponse) => {
        this.handleIncomingMessage(messageResponse, myId);
      },
      error: (err) => console.error('[ChatDock] Realtime WebSocket error:', err),
    });
  }

  private syncTypingSubscriptions(conversationIds: string[], myId: string): void {
    for (const id of conversationIds) {
      if (!this.typingSubs.has(id)) {
        const sub = this.messageService.watchTyping(id).subscribe({
          next: (event) => this.handleTypingEvent(event, myId),
          error: (err) => console.debug('[ChatDock] Typing error:', err),
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
    this.conversationCreatedSub?.unsubscribe();

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

  private updateConversation(id: string, updater: (conversation: Conversation) => Conversation): void {
    this.conversations.update((list) => list.map((c) => (c.id === id ? updater(c) : c)));
  }
}
