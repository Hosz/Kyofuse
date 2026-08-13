export type NotificationType =
  | 'FOLLOW_REQUEST_RECEIVED'
  | 'FOLLOW_REQUEST_ACCEPTED'
  | 'FOLLOW_REQUEST_DECLINED'
  | 'FOLLOW_STARTED'
  | 'TEAM_INVITE_RECEIVED'
  | 'TEAM_INVITE_ACCEPTED'
  | 'TEAM_INVITE_DECLINED'
  | 'TEAM_INVITE_CANCELED'
  | 'TEAM_MEMBER_ADDED'
  | 'TEAM_MEMBER_REMOVED'
  | 'TEAM_MEMBER_LEFT'
  | 'TEAM_MEMBER_EDITED'
  | 'NEW_POST'
  | 'POST_COMMENT'
  | 'POST_REACTION'
  | 'COMMENT_REACTION'
  | 'SYSTEM';

export type NotificationStatus = 'UNREAD' | 'READ' | 'ARCHIVED';

export type NotificationTargetType = 'POST' | 'COMMENT' | 'TEAM' | 'TEAM_INVITE' | 'FOLLOW' | 'SYSTEM';

export interface NotificationActorResponse {
    username: string;
    avatarUrl: string;
}

export interface NotificationTargetResponse {
    type: NotificationTargetType;
    id: string;
}

export interface NotificationResponse {
    id: string;
    type: NotificationType;
    title: string;
    message: string;
    status: NotificationStatus;
    createdAt: string;
    readAt: string | null;
    actor: NotificationActorResponse | null;
    target: NotificationTargetResponse | null;
    metadata: Record<string, unknown>;
}
