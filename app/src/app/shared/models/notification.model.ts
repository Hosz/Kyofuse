import { NotificationType as BackendNotificationType } from '../../models/notifications/notification.model';

export type NotificationType = BackendNotificationType;

/** 'social' = gerada por atividade de outros usuários. 'system' = gerada pela plataforma. */
export type NotificationSource = 'social' | 'system';

export type NotificationStatus = 'unread' | 'read' | 'archived';

export interface NotificationBodySegment {
  text: string;
  bold?: boolean;
}

export interface NotificationAction {
  acceptLabel: string;
  declineLabel: string;
}

export interface AppNotification {
  id: string;
  type: NotificationType;
  source: NotificationSource;
  title: string;
  timeAgo: string;
  /** Data crua, pra ordenar junto com itens que não vêm de notificações. */
  createdAt: string;
  body: NotificationBodySegment[];
  status: NotificationStatus;
  icon: string;
  avatarUrl?: string;
  action?: NotificationAction;
  /** Id do alvo (ex.: id da solicitação de follow) — usado pelas ações de aceitar/recusar. */
  targetId?: string;
  /** Preenchido quando a notificação aponta pra uma conversa, pra poder abri-la. */
  conversationId?: string;
  rawResponse?: import('../../models/notifications/notification.model').NotificationResponse;
}
