import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { NotificationCardComponent } from '../../components/notifications/notification-card/notification-card';
import { SkeletonComponent } from '../../components/shared/skeleton/skeleton';
import { InfiniteScrollDirective } from '../../shared/directives/infinite-scroll.directive';
import { AppNotification } from '../../shared/models/notification.model';
import { NotificationService } from '../../core/services/notifications/notification.service';
import { ToastService } from '../../core/services/ui/toast.service';
import { FollowService } from '../../core/services/follow/follow.service';
import { FriendshipService } from '../../core/services/friendship/friendship.service';
import { FriendRequestService } from '../../core/services/friendship/friend-request.service';
import { TeamInviteService } from '../../core/services/teams/team-invite.service';
import { NotificationResponse, NotificationType } from '../../models/notifications/notification.model';
import { FriendRequestResponse } from '../../models/friendship/friend-request.model';
import { toTimeAgo } from '../../shared/utils/format.util';

type FilterId = 'all' | 'unread' | 'read' | 'archived' | 'system';
type ViewMode = 'notifications' | 'requests';
type RequestTab = 'all' | 'friends' | 'follow' | 'teams' | 'messages';

/** Espelha o enum NotificationType do backend — cada tipo vira um ícone. */
const TYPE_ICON: Record<NotificationType, string> = {
  FOLLOW_REQUEST_RECEIVED: 'person_add',
  FOLLOW_REQUEST_ACCEPTED: 'how_to_reg',
  FOLLOW_REQUEST_DECLINED: 'person_off',
  FOLLOW_STARTED: 'person_add',
  TEAM_INVITE_RECEIVED: 'group_add',
  TEAM_INVITE_ACCEPTED: 'groups',
  TEAM_INVITE_DECLINED: 'group_off',
  TEAM_INVITE_CANCELED: 'group_off',
  TEAM_MEMBER_ADDED: 'group_add',
  TEAM_MEMBER_REMOVED: 'person_remove',
  TEAM_MEMBER_LEFT: 'logout',
  TEAM_MEMBER_EDITED: 'edit',
  NEW_POST: 'article',
  POST_COMMENT: 'chat_bubble',
  POST_REACTION: 'favorite',
  COMMENT_REACTION: 'favorite',
  NEW_MESSAGE: 'chat',
  MESSAGE_REQUEST: 'mark_unread_chat_alt',
  SYSTEM: 'info',
};

/**
 * Uma solicitação pode nascer de duas origens: da lista de notificações (seguir, time,
 * mensagem) ou do endpoint de pedidos de amizade. Elas são normalizadas nesse item pra
 * caberem numa lista só, ordenada por data como a de notificações.
 */
interface RequestItem {
  key: string;
  tab: Exclude<RequestTab, 'all'>;
  createdAt: string;
  notification: AppNotification;
  /** Só nos pedidos de amizade — o aceite deles usa outro serviço. */
  friendRequest?: FriendRequestResponse;
}

const REQUESTS_EMPTY_MESSAGE: Record<RequestTab, string> = {
  all: 'Nenhuma solicitação pendente.',
  friends: 'Nenhuma solicitação de amizade pendente.',
  follow: 'Nenhuma solicitação para seguir pendente.',
  teams: 'Nenhum convite de time pendente.',
  messages: 'Nenhuma solicitação de mensagem.',
};

const EMPTY_MESSAGE: Record<FilterId, string> = {
  all: 'Nenhuma notificação por aqui ainda.',
  unread: 'Nenhuma notificação não lida.',
  read: 'Nenhuma notificação lida.',
  archived: 'Nenhuma notificação arquivada.',
  system: 'Nenhuma notificação do sistema.',
};

@Component({
  selector: 'app-notifications',
  imports: [AppSidebarComponent, NotificationCardComponent, SkeletonComponent, InfiniteScrollDirective],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css',
})
export class NotificationsComponent {
  private notificationService = inject(NotificationService);
  private followService = inject(FollowService);
  private friendshipService = inject(FriendshipService);
  private friendRequestService = inject(FriendRequestService);
  private teamInviteService = inject(TeamInviteService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  private notifications = signal<AppNotification[]>([]);
  private page = signal(0);
  private lastPage = signal(true);

  loading = signal(true);
  activeFilter = signal<FilterId>('all');

  view = signal<ViewMode>('notifications');
  requestTab = signal<RequestTab>('all');

  filters: { id: FilterId; label: string }[] = [
    { id: 'all', label: 'Todas' },
    { id: 'unread', label: 'Não lidas' },
    { id: 'read', label: 'Lidas' },
    { id: 'archived', label: 'Arquivadas' },
    { id: 'system', label: 'Sistema' },
  ];

  requestTabs: { id: RequestTab; label: string }[] = [
    { id: 'all', label: 'Todas' },
    { id: 'friends', label: 'Amizade' },
    { id: 'follow', label: 'Seguir' },
    { id: 'teams', label: 'Times' },
    { id: 'messages', label: 'Mensagens' },
  ];

  friendRequests = signal<FriendRequestResponse[]>([]);
  friendRequestsLoading = signal(true);
  friendRequestsLoadingMore = signal(false);
  private friendRequestsPendingIds = signal<Set<string>>(new Set());
  private friendRequestsPage = signal(0);
  private friendRequestsLastPage = signal(true);

  hasMoreFriendRequests = computed(() => !this.friendRequestsLastPage());

  unreadCount = computed(() => this.notifications().filter((n) => n.status === 'unread').length);

  /** Solicitações de conversa: o backend marca a primeira mensagem de uma conversa
   * ainda pendente como MESSAGE_REQUEST, então basta filtrar por esse tipo. O aceite
   * acontece na própria conversa, pra onde o clique leva. */
  messageRequests = computed(() =>
    this.notifications().filter((n) => n.type === 'MESSAGE_REQUEST'),
  );

  /** Pedidos de follow ainda pendentes: derivados da própria lista de notificações
   * (não existe endpoint dedicado pra listar solicitações de follow recebidas). */
  followRequests = computed(() =>
    this.notifications().filter((n) => n.type === 'FOLLOW_REQUEST_RECEIVED' && !!n.action),
  );

  /** Convites de time ainda pendentes: mesma lógica, derivados das notificações
   * (não existe endpoint pra listar convites recebidos por mim em todos os times). */
  teamInviteRequests = computed(() =>
    this.notifications().filter((n) => n.type === 'TEAM_INVITE_RECEIVED' && !!n.action),
  );

  /** Pedidos de amizade viram notificações sintéticas pra renderizarem no mesmo card das
   * demais solicitações — visualmente não há motivo pra elas destoarem. */
  private friendRequestItems = computed<RequestItem[]>(() =>
    this.friendRequests().map((request) => ({
      key: `friend:${request.id}`,
      tab: 'friends' as const,
      createdAt: request.createdAt,
      friendRequest: request,
      notification: {
        id: request.id,
        type: 'FOLLOW_REQUEST_RECEIVED',
        source: 'social',
        title: 'Solicitação de amizade',
        timeAgo: toTimeAgo(request.createdAt),
        createdAt: request.createdAt,
        status: 'unread',
        icon: 'person_add',
        body: [
          { text: '@' + request.senderUsername, bold: true },
          { text: ' quer ser seu amigo' },
        ],
        action: { acceptLabel: 'Aceitar', declineLabel: 'Recusar' },
      },
    })),
  );

  /** Tudo junto e em ordem cronológica, como na aba de notificações: separar por tópico
   * atrapalha quando há muita solicitação acumulada. */
  private allRequests = computed<RequestItem[]>(() => {
    const fromNotifications = (list: AppNotification[], tab: Exclude<RequestTab, 'all'>) =>
      list.map((notification) => ({
        key: `${tab}:${notification.id}`,
        tab,
        createdAt: notification.createdAt,
        notification,
      }));

    return [
      ...this.friendRequestItems(),
      ...fromNotifications(this.followRequests(), 'follow'),
      ...fromNotifications(this.teamInviteRequests(), 'teams'),
      ...fromNotifications(this.messageRequests(), 'messages'),
    ].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  });

  visibleRequests = computed(() => {
    const tab = this.requestTab();
    return tab === 'all' ? this.allRequests() : this.allRequests().filter((item) => item.tab === tab);
  });

  requestsLoading = computed(() => this.loading() || this.friendRequestsLoading());

  requestsEmptyMessage = computed(() => REQUESTS_EMPTY_MESSAGE[this.requestTab()]);

  /** A paginação de amizade só faz sentido enquanto esses itens estão em tela. */
  showLoadMoreFriendRequests = computed(
    () => this.hasMoreFriendRequests() && (this.requestTab() === 'all' || this.requestTab() === 'friends'),
  );

  pendingRequestsCount = computed(
    () => this.friendRequests().length + this.followRequests().length + this.teamInviteRequests().length,
  );

  filteredNotifications = computed(() => {
    const list = this.notifications();
    switch (this.activeFilter()) {
      case 'unread':
        return list.filter((n) => n.status === 'unread');
      case 'read':
        return list.filter((n) => n.status === 'read');
      case 'archived':
        return list.filter((n) => n.status === 'archived');
      case 'system':
        return list.filter((n) => n.source === 'system');
      default:
        return list.filter((n) => n.status !== 'archived');
    }
  });

  emptyMessage = computed(() => EMPTY_MESSAGE[this.activeFilter()]);

  hasMoreToLoad = computed(() => !this.lastPage());

  constructor() {
    /** Mantém a bolinha de contagem da sidebar em dia enquanto essa página está aberta,
     * sem precisar de uma chamada extra à API a cada ação de ler/arquivar. */
    effect(() => this.notificationService.unreadCount.set(this.unreadCount()));
  }

  ngOnInit(): void {
    this.loadPage(0);
    this.loadFriendRequests();
    this.notificationService.newNotification$.subscribe({
      next: (notification) => {
        const appNotif = this.toAppNotification(notification);
        this.notifications.update((list) => {
          if (list.some((n) => n.id === appNotif.id)) return list;
          return [appNotif, ...list];
        });
      },
    });
  }

  setView(view: ViewMode): void {
    this.view.set(view);
  }

  openConversation(notification: AppNotification): void {
    if (!notification.conversationId) return;
    if (notification.status === 'unread') this.toggleRead(notification);
    this.router.navigate(['/chats', notification.conversationId]);
  }

  setRequestTab(tab: RequestTab): void {
    this.requestTab.set(tab);
  }

  setFilter(id: FilterId): void {
    this.activeFilter.set(id);
  }

  acceptRequest(item: RequestItem): void {
    if (item.friendRequest) this.acceptFriendRequest(item.friendRequest);
    else this.accept(item.notification);
  }

  declineRequest(item: RequestItem): void {
    if (item.friendRequest) this.declineFriendRequest(item.friendRequest);
    else this.decline(item.notification);
  }

  /** Só a solicitação de mensagem leva a algum lugar: o aceite dela mora na conversa. */
  openRequest(item: RequestItem): void {
    if (item.tab === 'messages') this.openConversation(item.notification);
  }

  isRequestPending(item: RequestItem): boolean {
    return !!item.friendRequest && this.isFriendRequestPending(item.friendRequest.id);
  }

  toggleRead(notification: AppNotification): void {
    if (notification.status !== 'unread') return;

    this.notificationService.readNotification(notification.id).subscribe({
      next: () => this.updateNotification(notification.id, (n) => ({ ...n, status: 'read' })),
      error: (error) => {
        console.error('Failed to mark notification as read:', error);
        this.toastService.error('Não foi possível marcar como lida.');
      },
    });
  }

  toggleArchive(notification: AppNotification): void {
    if (notification.status === 'archived') return;

    this.notificationService.archiveNotification(notification.id).subscribe({
      next: () => this.updateNotification(notification.id, (n) => ({ ...n, status: 'archived' })),
      error: (error) => {
        console.error('Failed to archive notification:', error);
        this.toastService.error('Não foi possível arquivar a notificação.');
      },
    });
  }

  accept(notification: AppNotification): void {
    const targetId = notification.targetId;
    if (!targetId) return;

    const onSuccess = () => this.updateNotification(notification.id, (n) => ({ ...n, status: 'read', action: undefined }));
    const onError = (error: unknown) => {
      console.error('Failed to accept request:', error);
      this.toastService.error('Não foi possível aceitar a solicitação.');
    };

    if (notification.type === 'TEAM_INVITE_RECEIVED') {
      this.teamInviteService.acceptInvite(targetId).subscribe({ next: onSuccess, error: onError });
    } else {
      this.followService.acceptFollowRequest(targetId).subscribe({ next: onSuccess, error: onError });
    }
  }

  decline(notification: AppNotification): void {
    const targetId = notification.targetId;
    if (!targetId) return;

    const onSuccess = () => this.updateNotification(notification.id, (n) => ({ ...n, status: 'archived', action: undefined }));
    const onError = (error: unknown) => {
      console.error('Failed to decline request:', error);
      this.toastService.error('Não foi possível recusar a solicitação.');
    };

    if (notification.type === 'TEAM_INVITE_RECEIVED') {
      this.teamInviteService.declineInvite(targetId).subscribe({ next: onSuccess, error: onError });
    } else {
      this.followService.rejectFollowRequest(targetId).subscribe({ next: onSuccess, error: onError });
    }
  }

  markAllAsRead(): void {
    this.notificationService.readAllNotifications().subscribe({
      next: () => {
        this.notifications.update((list) => list.map((n) => (n.status === 'unread' ? { ...n, status: 'read' } : n)));
      },
      error: (error) => console.error('Failed to mark all notifications as read:', error),
    });
  }

  loadingMore = signal(false);

  loadPrevious(): void {
    if (this.loadingMore() || this.lastPage()) return;
    this.loadingMore.set(true);
    this.loadPage(this.page() + 1);
  }

  isFriendRequestPending(requestId: string): boolean {
    return this.friendRequestsPendingIds().has(requestId);
  }

  acceptFriendRequest(request: FriendRequestResponse): void {
    if (this.isFriendRequestPending(request.id)) return;
    this.setFriendRequestPending(request.id, true);

    this.friendshipService.acceptRequest(request.id).subscribe({
      next: () => {
        this.removeFriendRequest(request.id);
        this.toastService.success('Solicitação de amizade aceita com sucesso!');
      },
      error: (error) => {
        console.error('Failed to accept friend request:', error);
        this.setFriendRequestPending(request.id, false);
        this.toastService.error(error?.error?.message ?? 'Não foi possível aceitar a solicitação.');
      },
    });
  }

  declineFriendRequest(request: FriendRequestResponse): void {
    if (this.isFriendRequestPending(request.id)) return;
    this.setFriendRequestPending(request.id, true);

    this.friendshipService.declineRequest(request.id).subscribe({
      next: () => {
        this.removeFriendRequest(request.id);
        this.toastService.info('Solicitação de amizade recusada.');
      },
      error: (error) => {
        console.error('Failed to decline friend request:', error);
        this.setFriendRequestPending(request.id, false);
        this.toastService.error(error?.error?.message ?? 'Não foi possível recusar a solicitação.');
      },
    });
  }

  loadMoreFriendRequests(): void {
    if (this.friendRequestsLoadingMore() || !this.hasMoreFriendRequests()) return;
    this.loadFriendRequests(this.friendRequestsPage() + 1);
  }

  private loadFriendRequests(page: number = 0): void {
    if (page === 0) this.friendRequestsLoading.set(true);
    else this.friendRequestsLoadingMore.set(true);

    this.friendRequestService.showReceivedRequests(page).subscribe({
      next: (response) => {
        this.friendRequests.update((list) => (page === 0 ? response.content : [...list, ...response.content]));
        this.friendRequestsPage.set(page);
        this.friendRequestsLastPage.set(response.last);
        this.friendRequestsLoading.set(false);
        this.friendRequestsLoadingMore.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch friend requests:', error);
        this.friendRequestsLoading.set(false);
        this.friendRequestsLoadingMore.set(false);
      },
    });
  }

  private removeFriendRequest(id: string): void {
    this.friendRequests.update((list) => list.filter((r) => r.id !== id));
    this.setFriendRequestPending(id, false);
  }

  private setFriendRequestPending(id: string, pending: boolean): void {
    this.friendRequestsPendingIds.update((ids) => {
      const next = new Set(ids);
      if (pending) next.add(id);
      else next.delete(id);
      return next;
    });
  }

  private loadPage(page: number): void {
    this.notificationService.listNotifications(page).subscribe({
      next: (response) => {
        const mapped = response.content.map((notification) => this.toAppNotification(notification));
        this.notifications.update((list) => (page === 0 ? mapped : [...list, ...mapped]));
        this.page.set(page);
        this.lastPage.set(response.last);
        this.loading.set(false);
        this.loadingMore.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch notifications:', error);
        this.loading.set(false);
        this.loadingMore.set(false);
        this.toastService.error('Não foi possível carregar as notificações.');
      },
    });
  }

  private updateNotification(id: string, updater: (n: AppNotification) => AppNotification): void {
    this.notifications.update((list) => list.map((n) => (n.id === id ? updater(n) : n)));
  }

  private toAppNotification(n: NotificationResponse): AppNotification {
    const isActionable = n.type === 'FOLLOW_REQUEST_RECEIVED' || n.type === 'TEAM_INVITE_RECEIVED';
    // Solicitação de mensagem não tem aceite aqui: o Aceitar/Recusar vive na própria
    // conversa, então guardamos o id dela pra poder navegar até lá.
    const conversationId = n.target?.type === 'CONVERSATION' ? n.target.id : undefined;

    return {
      id: n.id,
      type: n.type,
      source: n.type === 'SYSTEM' ? 'system' : 'social',
      title: n.title,
      timeAgo: toTimeAgo(n.createdAt),
      createdAt: n.createdAt,
      status: n.status === 'UNREAD' ? 'unread' : n.status === 'READ' ? 'read' : 'archived',
      icon: TYPE_ICON[n.type],
      avatarUrl: n.actor?.avatarUrl,
      body: [{ text: n.message }],
      action: isActionable ? { acceptLabel: 'Aceitar', declineLabel: 'Recusar' } : undefined,
      targetId: isActionable ? n.target?.id : undefined,
      conversationId,
    };
  }
}
