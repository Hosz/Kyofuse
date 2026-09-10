import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
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
import { I18nService } from '../../core/i18n/i18n.service';

type FilterId = 'all' | 'archived' | 'system';
type ViewMode = 'notifications' | 'requests';
type RequestTab = 'all' | 'friends' | 'follow' | 'teams' | 'messages';

const TYPE_TITLE_KEY: Partial<Record<NotificationType, string>> = {
  FOLLOW_REQUEST_RECEIVED: 'notifications.typeFollowRequestReceived',
  FOLLOW_REQUEST_ACCEPTED: 'notifications.typeFollowRequestAccepted',
  FOLLOW_REQUEST_DECLINED: 'notifications.typeFollowRequestDeclined',
  FOLLOW_STARTED: 'notifications.typeFollowStarted',
  TEAM_INVITE_RECEIVED: 'notifications.typeTeamInviteReceived',
  TEAM_INVITE_ACCEPTED: 'notifications.typeTeamInviteAccepted',
  TEAM_INVITE_DECLINED: 'notifications.typeTeamInviteDeclined',
  TEAM_INVITE_CANCELED: 'notifications.typeTeamInviteCanceled',
  TEAM_MEMBER_ADDED: 'notifications.typeTeamMemberAdded',
  TEAM_MEMBER_REMOVED: 'notifications.typeTeamMemberRemoved',
  TEAM_MEMBER_LEFT: 'notifications.typeTeamMemberLeft',
  TEAM_MEMBER_EDITED: 'notifications.typeTeamMemberEdited',
  NEW_POST: 'notifications.typeNewPost',
  POST_COMMENT: 'notifications.typePostComment',
  POST_REACTION: 'notifications.typePostReaction',
  COMMENT_REACTION: 'notifications.typeCommentReaction',
  NEW_MESSAGE: 'notifications.typeNewMessage',
  MESSAGE_REQUEST: 'notifications.typeMessageRequest',
  SYSTEM: 'notifications.typeSystem',
};

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
  archived: 'Nenhuma notificação arquivada.',
  system: 'Nenhuma notificação do sistema.',
};

import { TranslatePipe } from '../../core/i18n/translate.pipe';

@Component({
  selector: 'app-notifications',
  imports: [AppSidebarComponent, NotificationCardComponent, SkeletonComponent, InfiniteScrollDirective, TranslatePipe],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css',
})
export class NotificationsComponent {
  readonly i18n = inject(I18nService);
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

  filters: { id: FilterId; labelKey: string }[] = [
    { id: 'all', labelKey: 'notifications.filterAll' },
    { id: 'archived', labelKey: 'notifications.filterArchived' },
    { id: 'system', labelKey: 'notifications.filterSystem' },
  ];

  requestTabs: { id: RequestTab; labelKey: string }[] = [
    { id: 'all', labelKey: 'notifications.tabAll' },
    { id: 'friends', labelKey: 'notifications.tabFriends' },
    { id: 'follow', labelKey: 'notifications.tabFollow' },
    { id: 'teams', labelKey: 'notifications.tabTeams' },
    { id: 'messages', labelKey: 'notifications.tabMessages' },
  ];

  friendRequests = signal<FriendRequestResponse[]>([]);
  friendRequestsLoading = signal(true);
  friendRequestsLoadingMore = signal(false);
  private friendRequestsPendingIds = signal<Set<string>>(new Set());
  private friendRequestsPage = signal(0);
  private friendRequestsLastPage = signal(true);

  hasMoreFriendRequests = computed(() => !this.friendRequestsLastPage());

  readonly unreadCount = this.notificationService.unreadCount;

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
  private friendRequestItems = computed<RequestItem[]>(() => {
    const lang = this.i18n.currentLang();
    return this.friendRequests().map((request) => ({
      key: `friend:${request.id}`,
      tab: 'friends' as const,
      createdAt: request.createdAt,
      friendRequest: request,
      notification: {
        id: request.id,
        type: 'FOLLOW_REQUEST_RECEIVED',
        source: 'social',
        title: this.i18n.t('notifications.typeFriendRequest'),
        timeAgo: toTimeAgo(request.createdAt, lang),
        createdAt: request.createdAt,
        status: 'unread',
        icon: 'person_add',
        body: [
          { text: '@' + request.senderUsername, bold: true },
          { text: ' ' + this.i18n.t('notifications.wantsToBeFriends') },
        ],
        action: {
          acceptLabel: this.i18n.t('common.accept'),
          declineLabel: this.i18n.t('common.decline'),
        },
      },
    }));
  });

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

    // Encontra o ID do alerta de login lido mais recente
    const latestReadLoginAlert = list.find((n) => n.isLoginAlert && n.status === 'read');
    const latestReadLoginId = latestReadLoginAlert?.id;

    // Estilo Instagram: mantém todos os alertas de login não lidos, mas apenas o último lido.
    // Alertas de login lidos mais antigos são omitidos da listagem principal.
    const deduplicated = list.filter((n) => {
      if (!n.isLoginAlert) return true;
      if (n.status === 'unread') return true;
      if (n.status === 'read') return n.id === latestReadLoginId;
      return true;
    });

    switch (this.activeFilter()) {
      case 'archived':
        return deduplicated.filter((n) => n.status === 'archived');
      case 'system':
        return deduplicated.filter((n) => n.source === 'system' && n.status !== 'archived');
      default:
        return deduplicated.filter((n) => n.status !== 'archived');
    }
  });

  emptyMessage = computed(() => EMPTY_MESSAGE[this.activeFilter()]);

  hasMoreToLoad = computed(() => !this.lastPage());

  constructor() {
    effect(() => {
      const lang = this.i18n.currentLang();
      untracked(() => {
        this.refreshNotificationTranslations(lang);
      });
    });
  }

  private refreshNotificationTranslations(lang: string): void {
    this.notifications.update((list) =>
      list.map((n) => {
        const titleKey = TYPE_TITLE_KEY[n.type];
        const isActionable = (n.type === 'FOLLOW_REQUEST_RECEIVED' || n.type === 'TEAM_INVITE_RECEIVED') && !!n.action;
        return {
          ...n,
          title: titleKey ? this.i18n.t(titleKey) : n.title,
          timeAgo: toTimeAgo(n.createdAt, lang),
          body: n.rawResponse ? this.getNotificationBody(n.rawResponse) : n.body,
          action: isActionable
            ? { acceptLabel: this.i18n.t('common.accept'), declineLabel: this.i18n.t('common.decline') }
            : undefined,
        };
      })
    );
  }

  ngOnInit(): void {
    // Ao entrar na área de notificações, marca automaticamente todas como lidas
    this.notificationService.readAllNotifications().subscribe({
      next: () => {
        this.notificationService.unreadCount.set(0);
        this.notifications.update((list) =>
          list.map((n) => (n.status === 'unread' ? { ...n, status: 'read' } : n))
        );
      },
      error: (error) => console.error('Failed to mark all as read on enter:', error),
    });

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
      next: () => {
        this.updateNotification(notification.id, (n) => ({ ...n, status: 'read' }));
        this.notificationService.unreadCount.update((c) => Math.max(0, c - 1));
      },
      error: (error) => {
        console.error('Failed to mark notification as read:', error);
        this.toastService.error('Não foi possível marcar como lida.');
      },
    });
  }

  toggleArchive(notification: AppNotification): void {
    if (notification.status === 'archived') return;

    this.notificationService.archiveNotification(notification.id).subscribe({
      next: () => {
        const wasUnread = notification.status === 'unread';
        this.updateNotification(notification.id, (n) => ({ ...n, status: 'archived' }));
        if (wasUnread) {
          this.notificationService.unreadCount.update((c) => Math.max(0, c - 1));
        }
      },
      error: (error) => {
        console.error('Failed to archive notification:', error);
        this.toastService.error('Não foi possível arquivar a notificação.');
      },
    });
  }

  accept(notification: AppNotification): void {
    const targetId = notification.targetId;
    if (!targetId) return;

    const nextType: NotificationType =
      notification.type === 'TEAM_INVITE_RECEIVED' ? 'TEAM_INVITE_ACCEPTED' : 'FOLLOW_REQUEST_ACCEPTED';
    const successToast =
      notification.type === 'TEAM_INVITE_RECEIVED'
        ? this.i18n.t('notifications.teamInviteAcceptedSuccess')
        : this.i18n.t('notifications.followRequestAcceptedSuccess');

    const onSuccess = () => {
      this.updateNotification(notification.id, (n) => ({
        ...n,
        type: nextType,
        title: this.i18n.t(TYPE_TITLE_KEY[nextType] ?? ''),
        icon: TYPE_ICON[nextType] ?? n.icon,
        status: 'read',
        action: undefined,
        responseStatus: 'accepted',
        rawResponse: n.rawResponse
          ? {
              ...n.rawResponse,
              type: nextType,
              status: 'READ',
            }
          : undefined,
      }));
      this.toastService.success(successToast);
    };

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

    const nextType: NotificationType =
      notification.type === 'TEAM_INVITE_RECEIVED' ? 'TEAM_INVITE_DECLINED' : 'FOLLOW_REQUEST_DECLINED';
    const infoToast =
      notification.type === 'TEAM_INVITE_RECEIVED'
        ? this.i18n.t('notifications.teamInviteDeclinedInfo')
        : this.i18n.t('notifications.followRequestDeclinedInfo');

    const onSuccess = () => {
      this.updateNotification(notification.id, (n) => ({
        ...n,
        type: nextType,
        title: this.i18n.t(TYPE_TITLE_KEY[nextType] ?? ''),
        icon: TYPE_ICON[nextType] ?? n.icon,
        status: 'read',
        action: undefined,
        responseStatus: 'declined',
        rawResponse: n.rawResponse
          ? {
              ...n.rawResponse,
              type: nextType,
              status: 'READ',
            }
          : undefined,
      }));
      this.toastService.info(infoToast);
    };

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

  archiveAll(): void {
    this.notificationService.archiveAllNotifications().subscribe({
      next: () => {
        this.notifications.update((list) =>
          list.map((n) => ({ ...n, status: 'archived' }))
        );
        this.notificationService.unreadCount.set(0);
        this.toastService.success(this.i18n.t('notifications.allArchivedSuccess'));
      },
      error: (error) => {
        console.error('Failed to archive all notifications:', error);
        this.toastService.error('Não foi possível arquivar as notificações.');
      },
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
        const mapped = response.content.map((notification) => {
          const appNotif = this.toAppNotification(notification);
          if (page === 0 && appNotif.status === 'unread') {
            return { ...appNotif, status: 'read' as const };
          }
          return appNotif;
        });
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
    const titleKey = TYPE_TITLE_KEY[n.type];

    const isLoginAlert = n.type === 'SYSTEM' && (
      n.title === 'Novo login detectado' ||
      (n.message && n.message.includes('Sua conta foi acessada em')) ||
      !!n.metadata?.['location']
    );

    const title = isLoginAlert
      ? this.i18n.t('notifications.loginAlertTitle')
      : (titleKey ? this.i18n.t(titleKey) : n.title);

    const body = isLoginAlert
      ? this.formatLoginAlertBody(n)
      : this.getNotificationBody(n);

    const responseStatus: 'accepted' | 'declined' | undefined =
      n.type === 'TEAM_INVITE_ACCEPTED' || n.type === 'FOLLOW_REQUEST_ACCEPTED'
        ? 'accepted'
        : n.type === 'TEAM_INVITE_DECLINED' || n.type === 'FOLLOW_REQUEST_DECLINED'
          ? 'declined'
          : undefined;

    return {
      id: n.id,
      type: n.type,
      source: n.type === 'SYSTEM' ? 'system' : 'social',
      title,
      timeAgo: toTimeAgo(n.createdAt, this.i18n.currentLang()),
      createdAt: n.createdAt,
      status: n.status === 'UNREAD' ? 'unread' : n.status === 'READ' ? 'read' : 'archived',
      icon: TYPE_ICON[n.type] ?? 'notifications',
      avatarUrl: n.actor?.avatarUrl,
      body,
      action: isActionable
        ? { acceptLabel: this.i18n.t('common.accept'), declineLabel: this.i18n.t('common.decline') }
        : undefined,
      targetId: isActionable ? n.target?.id : undefined,
      conversationId,
      responseStatus,
      isLoginAlert,
      rawResponse: n,
    };
  }

  private formatLoginAlertBody(n: NotificationResponse): { text: string; bold?: boolean }[] {
    const match = n.message?.match(/Sua conta foi acessada em (.*?) usando (.*?)\.?$/i);
    const location = (n.metadata?.['location'] as string) || (match ? match[1] : '');
    const device = (n.metadata?.['device'] as string) || (match ? match[2] : '');
    const localized = this.i18n.t('notifications.loginAlertMessage')
      .replace('{location}', location)
      .replace('{device}', device);
    return [{ text: localized }];
  }

  private getNotificationBody(n: NotificationResponse): { text: string; bold?: boolean }[] {
    if (n.actor?.username) {
      const username = '@' + n.actor.username;
      switch (n.type) {
        case 'FOLLOW_REQUEST_RECEIVED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.requestedToFollowYou') }];
        case 'FOLLOW_STARTED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.startedFollowingYou') }];
        case 'FOLLOW_REQUEST_ACCEPTED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.acceptedFollowRequest') }];
        case 'FOLLOW_REQUEST_DECLINED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.requestedToFollowYou') }];
        case 'POST_COMMENT':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.commentedOnPost') }];
        case 'TEAM_INVITE_RECEIVED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.invitedYouToTeam') }];
        case 'TEAM_INVITE_ACCEPTED':
          if (n.message && (n.message.includes('Aceitou') || n.message.includes('accepted'))) {
            return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.joinedTeam') }];
          }
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.invitedYouToTeam') }];
        case 'TEAM_INVITE_DECLINED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.invitedYouToTeam') }];
        case 'TEAM_INVITE_CANCELED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.inviteToTeamCanceled') }];
        case 'TEAM_MEMBER_ADDED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.joinedTeam') }];
        case 'TEAM_MEMBER_LEFT':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.leftTeam') }];
        case 'TEAM_MEMBER_REMOVED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.removedFromTeam') }];
        case 'TEAM_MEMBER_EDITED':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.roleChangedInTeam') }];
        case 'NEW_MESSAGE':
        case 'MESSAGE_REQUEST':
          return [{ text: username, bold: true }, { text: ' ' + this.i18n.t('notifications.sentMessage') }];
      }
    }
    return [{ text: n.message }];
  }
}
