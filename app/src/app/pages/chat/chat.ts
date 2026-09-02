import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ConversationListComponent } from '../../components/chat/conversation-list/conversation-list';
import { ChatWindowComponent } from '../../components/chat/chat-window/chat-window';
import { NewConversationModalComponent } from '../../components/chat/new-conversation-modal/new-conversation-modal';
import { Conversation } from '../../shared/models/chat.model';
import { ConversationService } from '../../core/services/chat/conversation.service';
import { MessageService } from '../../core/services/chat/message.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { ToastService } from '../../core/services/ui/toast.service';
import { ConversationResponse } from '../../models/chat/chat.model';
import { PostMediaItemRequest } from '../../models/posts/post-request.model';
import { toChatMessage, toConversation } from '../../shared/utils/mappers.util';

const CONVERSATIONS_PAGE_SIZE = 50;
const MESSAGES_PAGE_SIZE = 50;

type ModalKind = 'new-conversation' | null;

@Component({
  selector: 'app-chat',
  imports: [AppSidebarComponent, ConversationListComponent, ChatWindowComponent, NewConversationModalComponent],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class ChatComponent {
  private readonly router = inject(Router);
  private readonly conversationService = inject(ConversationService);
  private readonly messageService = inject(MessageService);
  private readonly profileService = inject(ProfileService);
  private readonly toastService = inject(ToastService);

  /** Vinculado automaticamente ao parâmetro de rota :chatId (withComponentInputBinding). */
  chatId = input<string | null>(null);

  private readonly myUserId = signal<string | null>(null);
  private readonly conversations = signal<Conversation[]>([]);
  private readonly loadedMessagesFor = new Set<string>();

  activeModal = signal<ModalKind>(null);

  /** Exposto pro template: o chat-window precisa saber quem sou eu pra decidir se
   * mostro as ações de admin no painel de membros do grupo. */
  myUserIdValue = computed(() => this.myUserId());

  selectedId = computed(() => this.chatId() ?? this.conversations()[0]?.id ?? null);

  allConversations = computed(() => this.conversations());

  selectedConversation = computed<Conversation | null>(
    () => this.conversations().find((conversation) => conversation.id === this.selectedId()) ?? null,
  );

  constructor() {
    this.profileService.myProfile().subscribe({
      next: (profile) => {
        this.myUserId.set(profile.userId);
        this.loadConversations();
      },
      error: (error) => console.error('Failed to fetch my profile:', error),
    });

    // Roda de novo sempre que a conversa selecionada mudar (troca de rota ou seleção
    // na lista) — carrega o histórico de mensagens só na primeira vez que é aberta.
    // Só dispara depois que myUserId resolve, senão um link direto pra /chats/:id
    // (chatId já vem preenchido antes do myProfile responder) marcaria a conversa
    // como "carregada" sem nunca ter conseguido montar as mensagens.
    effect(() => {
      const conversationId = this.selectedId();
      const myId = this.myUserId();
      if (conversationId && myId) this.ensureMessagesLoaded(conversationId, myId);
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

  onSendMessage(payload: { content?: string; media?: PostMediaItemRequest[] }): void {
    const conversation = this.selectedConversation();
    if (!conversation) return;

    this.messageService.sendMessage(conversation.id, payload).subscribe({
      next: (response) => {
        const myId = this.myUserId();
        if (!myId) return;
        const message = toChatMessage(response, myId);
        this.updateConversation(conversation.id, (c) => ({
          ...c,
          lastMessageAt: 'Agora',
          messages: [...c.messages, message],
        }));
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
          (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime(),
        );
        this.conversations.set(responses.map((response) => this.toUiConversation(response)));
      },
      error: (error) => console.error('Failed to fetch conversations:', error),
    });
  }

  private ensureMessagesLoaded(conversationId: string, myId: string): void {
    if (this.loadedMessagesFor.has(conversationId)) return;
    this.loadedMessagesFor.add(conversationId);

    this.messageService.getMessages(conversationId, 0, MESSAGES_PAGE_SIZE).subscribe({
      next: (response) => {
        const messages = response.content
          .slice()
          .reverse()
          .map((message) => toChatMessage(message, myId));
        this.updateConversation(conversationId, (c) => ({ ...c, messages }));
      },
      error: (error) => {
        console.error('Failed to fetch messages:', error);
        this.loadedMessagesFor.delete(conversationId);
      },
    });
  }

  /** Preserva as mensagens já carregadas — a resposta desses endpoints é só o
   * envelope da conversa, não vem com o histórico de mensagens junto. */
  private replaceConversation(response: ConversationResponse): void {
    const mapped = this.toUiConversation(response);
    this.updateConversation(response.id, (c) => ({ ...mapped, messages: c.messages }));
  }

  private upsertConversation(response: ConversationResponse): void {
    const mapped = this.toUiConversation(response);
    this.conversations.update((list) => {
      const exists = list.some((c) => c.id === mapped.id);
      return exists ? list.map((c) => (c.id === mapped.id ? { ...mapped, messages: c.messages } : c)) : [mapped, ...list];
    });
  }

  private toUiConversation(response: ConversationResponse): Conversation {
    return toConversation(response, this.myUserId() ?? '');
  }

  private updateConversation(id: string, updater: (conversation: Conversation) => Conversation): void {
    this.conversations.update((list) => list.map((conversation) => (conversation.id === id ? updater(conversation) : conversation)));
  }
}
