import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { ModalComponent } from '../../shared/modal/modal';
import { UserOptionsMenuComponent } from '../../shared/user-options-menu/user-options-menu';
import { ConversationService } from '../../../core/services/chat/conversation.service';
import { ConversationMemberService } from '../../../core/services/chat/conversation-member.service';
import { ConversationMemberResponse, ConversationMemberRole, ConversationResponse } from '../../../models/chat/chat.model';
import { Conversation } from '../../../shared/models/chat.model';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';

interface ViewedMember {
  userId: string;
  username: string;
  nickname: string | null;
  avatarUrl: string | null;
  role: ConversationMemberRole;
}

const ROLE_LABEL: Record<ConversationMemberRole, string> = {
  ADMIN: 'Admin',
  MEMBER: 'Membro',
};

/** members = lista do grupo; member = cartão de um usuário; edit = formulário do grupo. */
type PanelView = 'members' | 'member' | 'edit';

@Component({
  selector: 'app-conversation-info-panel',
  imports: [RouterLink, ModalComponent, UserOptionsMenuComponent],
  templateUrl: './conversation-info-panel.html',
  styleUrl: './conversation-info-panel.css',
})
export class ConversationInfoPanelComponent {
  open = input(false);
  conversation = input<Conversation | null>(null);
  /** Id do usuário autenticado — usado pra saber se ele é ADMIN do grupo. */
  myUserId = input<string | null>(null);

  closed = output<void>();
  /** Emitido depois que o grupo é editado, pra a página do chat atualizar a lista. */
  groupUpdated = output<ConversationResponse>();

  private conversationService = inject(ConversationService);
  private conversationMemberService = inject(ConversationMemberService);

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  view = signal<PanelView>('members');
  viewedMember = signal<ViewedMember | null>(null);

  members = signal<ConversationMemberResponse[]>([]);
  membersLoading = signal(false);
  membersError = signal<string | null>(null);
  private loadedForConversationId: string | null = null;

  actionLoading = signal(false);
  actionError = signal<string | null>(null);

  editName = signal('');
  editAvatarUrl = signal('');
  saving = signal(false);
  editError = signal<string | null>(null);

  isGroup = computed(() => this.conversation()?.type === 'GROUP');
  isCommunity = computed(() => this.conversation()?.type === 'COMMUNITY');

  /** Só ADMIN ativo pode editar o grupo e mexer nos membros — mesma regra do backend. */
  iAmAdmin = computed(() => {
    const myUserId = this.myUserId();
    if (!this.isGroup() || !myUserId) return false;
    return this.members().some(
      (member) => member.userId === myUserId && member.role === 'ADMIN' && member.status === 'ACTIVE',
    );
  });

  /**
   * A listagem de membros não diz quem criou o grupo, então não dá pra esconder as
   * ações no criador aqui. O backend recusa remover ou rebaixar o criador de qualquer
   * jeito, e a mensagem dele aparece em actionError.
   */
  canManageViewedMember = computed(() => {
    const viewed = this.viewedMember();
    return this.iAmAdmin() && !!viewed && viewed.userId !== this.myUserId();
  });

  title = computed(() => {
    const viewed = this.viewedMember();
    if (this.view() === 'edit') return 'Editar grupo';
    if (this.view() === 'member' && viewed) return '@' + viewed.username;
    if (this.isGroup()) return 'Membros do grupo';
    if (this.isCommunity()) return 'Sobre a comunidade';
    return 'Sobre a conversa';
  });

  constructor() {
    effect(() => {
      const conversation = this.conversation();
      if (!this.open() || !conversation) return;

      if (conversation.type === 'DIRECT') {
        this.view.set('member');
        // Em DIRECT o participante já traz nickname (name) e foto vindos da listagem —
        // não há lista de membros de onde tirar isso, então reaproveita o que a conversa
        // carrega em vez de exibir o cartão sem imagem e sem apelido.
        this.viewedMember.set(
          conversation.participant.id
            ? {
                userId: conversation.participant.id,
                username: conversation.participant.handle,
                nickname: conversation.participant.name,
                avatarUrl: conversation.participant.avatarUrl,
                role: 'MEMBER',
              }
            : null,
        );
        return;
      }

      this.view.set('members');
      this.viewedMember.set(null);
      // COMMUNITY não tem conversation_members próprio (o acesso vem de
      // community_members) — listar membros aqui sempre daria 403. Quem quiser ver
      // os membros da comunidade é direcionado pra página dela (community.html), que
      // já lista isso corretamente via CommunityMemberService.
      if (conversation.type === 'GROUP') this.ensureMembersLoaded(conversation.id);
    });
  }

  roleLabel(role: ConversationMemberRole): string {
    return ROLE_LABEL[role] ?? role;
  }

  viewMember(member: ConversationMemberResponse): void {
    this.actionError.set(null);
    this.viewedMember.set({
      userId: member.userId,
      username: member.username,
      nickname: member.nickname,
      avatarUrl: member.avatarUrl,
      role: member.role,
    });
    this.view.set('member');
  }

  backToMembers(): void {
    this.actionError.set(null);
    this.viewedMember.set(null);
    this.view.set('members');
  }

  openEdit(): void {
    const conversation = this.conversation();
    if (!conversation) return;
    this.editName.set(conversation.participant.name);
    this.editAvatarUrl.set(conversation.participant.avatarUrl === FALLBACK_AVATAR_URL ? '' : conversation.participant.avatarUrl);
    this.editError.set(null);
    this.view.set('edit');
  }

  saveEdit(): void {
    const conversation = this.conversation();
    if (!conversation || this.saving()) return;
    if (!this.editName().trim()) {
      this.editError.set('O nome do grupo é obrigatório.');
      return;
    }

    this.saving.set(true);
    this.editError.set(null);

    this.conversationService
      .editGroupConversation(conversation.id, {
        name: this.editName().trim(),
        avatarUrl: this.editAvatarUrl().trim() || undefined,
      })
      .subscribe({
        next: (updated) => {
          this.saving.set(false);
          this.groupUpdated.emit(updated);
          this.view.set('members');
        },
        error: (error) => {
          console.error('Failed to edit group:', error);
          this.saving.set(false);
          this.editError.set(error?.error?.message ?? 'Não foi possível salvar as alterações do grupo.');
        },
      });
  }

  promoteMember(): void {
    const conversation = this.conversation();
    const viewed = this.viewedMember();
    if (!conversation || !viewed || this.actionLoading()) return;

    this.runMemberAction(this.conversationMemberService.promoteMemberToAdmin(conversation.id, viewed.userId));
  }

  demoteMember(): void {
    const conversation = this.conversation();
    const viewed = this.viewedMember();
    if (!conversation || !viewed || this.actionLoading()) return;

    this.runMemberAction(this.conversationMemberService.demoteAdminToMember(conversation.id, viewed.userId));
  }

  removeMember(): void {
    const conversation = this.conversation();
    const viewed = this.viewedMember();
    if (!conversation || !viewed || this.actionLoading()) return;

    this.runMemberAction(this.conversationMemberService.removeMember(conversation.id, viewed.userId));
  }

  onClose(): void {
    this.view.set('members');
    this.viewedMember.set(null);
    this.actionError.set(null);
    this.closed.emit();
  }

  private runMemberAction(request$: Observable<void>): void {
    this.actionLoading.set(true);
    this.actionError.set(null);

    request$.subscribe({
      next: () => {
        this.actionLoading.set(false);
        this.reloadMembers();
        this.backToMembers();
      },
      error: (error) => {
        console.error('Failed to run member action:', error);
        this.actionLoading.set(false);
        this.actionError.set(error?.error?.message ?? 'Não foi possível concluir essa ação.');
      },
    });
  }

  private reloadMembers(): void {
    const conversation = this.conversation();
    if (!conversation) return;
    this.loadedForConversationId = null;
    this.ensureMembersLoaded(conversation.id);
  }

  private ensureMembersLoaded(conversationId: string): void {
    if (this.loadedForConversationId === conversationId) return;
    this.loadedForConversationId = conversationId;
    this.membersLoading.set(true);
    this.membersError.set(null);

    this.conversationMemberService.listConversationMembers(conversationId).subscribe({
      next: (response) => {
        this.members.set(response.content.filter((member) => member.status === 'ACTIVE'));
        this.membersLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch conversation members:', error);
        this.membersError.set('Não foi possível carregar os membros do grupo.');
        this.membersLoading.set(false);
      },
    });
  }
}
