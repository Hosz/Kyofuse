import { Component, ElementRef, afterRenderEffect, computed, inject, signal, viewChild } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, forkJoin, map, startWith } from 'rxjs';
import { ConversationService } from '../../../core/services/chat/conversation.service';
import { MessageService } from '../../../core/services/chat/message.service';
import { ProfileService } from '../../../core/services/profile/profile.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { ConversationInfoPanelComponent } from '../conversation-info-panel/conversation-info-panel';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';
import { SkeletonComponent } from '../../shared/skeleton/skeleton';
import { ToastService } from '../../../core/services/ui/toast.service';
import { ConversationResponse } from '../../../models/chat/chat.model';
import { Conversation } from '../../../shared/models/chat.model';
import { toChatMessage, toChatMessageGroups, toConversation } from '../../../shared/utils/mappers.util';

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
  imports: [ConversationInfoPanelComponent, ConfirmDialogComponent, SkeletonComponent],
  templateUrl: './chat-dock.html',
  styleUrl: './chat-dock.css',
})
export class ChatDockComponent {
  private router = inject(Router);
  private conversationService = inject(ConversationService);
  private messageService = inject(MessageService);
  private profileService = inject(ProfileService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  private scroller = viewChild<ElementRef<HTMLElement>>('scroller');

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

  readonly filters: { value: DockFilter; label: string }[] = [
    { value: 'all', label: 'Geral' },
    { value: 'groups', label: 'Grupos' },
    { value: 'communities', label: 'Comunidades' },
  ];

  /** Conversa aberta dentro da janelinha. null = mostrando a lista. */
  activeId = signal<string | null>(null);
  messagesLoading = signal(false);
  sending = signal(false);
  draft = signal('');

  messagePendingDeletion = signal<string | null>(null);

  private myUserId = signal<string | null>(null);
  private loaded = false;
  private loadedMessagesFor = new Set<string>();

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

  messageGroups = computed(() => toChatMessageGroups(this.active()?.messages ?? []));

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
        return 'Abra a conversa para aceitar ou recusar a solicitação.';
      case 'request-sent':
        return 'Aguardando a outra pessoa aceitar sua solicitação.';
      default:
        return 'Esta conversa não está disponível.';
    }
  });

  constructor() {
    // Rola pro fim sempre que a conversa aberta muda ou recebe mensagem nova. Precisa
    // rodar depois do render: a altura só é a final quando as mensagens já estão no DOM.
    afterRenderEffect({
      write: () => {
        this.messageGroups();
        const element = this.scroller()?.nativeElement;
        if (element) element.scrollTop = element.scrollHeight;
      },
    });
  }

  setFilter(filter: DockFilter): void {
    this.activeFilter.set(filter);
  }

  onQueryChange(value: string): void {
    this.query.set(value);
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

  toggle(): void {
    const next = !this.open();
    this.open.set(next);
    if (next && !this.loaded) {
      this.loaded = true;
      this.load();
    }
  }

  close(): void {
    this.open.set(false);
  }

  openConversation(conversation: Conversation): void {
    this.activeId.set(conversation.id);
    this.draft.set('');
    const myId = this.myUserId();
    if (myId) this.ensureMessagesLoaded(conversation.id, myId);
  }

  back(): void {
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

    this.sending.set(true);

    this.messageService.sendMessage(conversation.id, { content }).subscribe({
      next: (response) => {
        this.draft.set('');
        this.sending.set(false);
        this.updateConversation(conversation.id, (c) => ({
          ...c,
          lastMessageAt: 'Agora',
          messages: [...c.messages, toChatMessage(response, myId)],
        }));
      },
      error: (error) => {
        console.error('Failed to send message:', error);
        this.sending.set(false);
        this.toastService.error('Não foi possível enviar a mensagem.');
      },
    });
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

  private ensureMessagesLoaded(conversationId: string, myId: string): void {
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
        this.updateConversation(conversationId, (c) => ({ ...c, messages }));
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
    this.loading.set(true);

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
              (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime(),
            );
            this.conversations.set(responses.map((response) => toConversation(response, profile.userId)));
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

  private updateConversation(id: string, updater: (conversation: Conversation) => Conversation): void {
    this.conversations.update((list) => list.map((c) => (c.id === id ? updater(c) : c)));
  }
}
