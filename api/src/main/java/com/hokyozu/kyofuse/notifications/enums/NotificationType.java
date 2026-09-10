package com.hokyozu.kyofuse.notifications.enums;

public enum NotificationType {
    FOLLOW_REQUEST_RECEIVED,
    FOLLOW_REQUEST_ACCEPTED,
    FOLLOW_REQUEST_DECLINED,

    FOLLOW_STARTED,

    TEAM_INVITE_RECEIVED,
    TEAM_INVITE_ACCEPTED,
    TEAM_INVITE_DECLINED,
    TEAM_INVITE_CANCELED,

    COMMUNITY_INVITE_RECEIVED,
    COMMUNITY_INVITE_ACCEPTED,
    COMMUNITY_INVITE_DECLINED,
    COMMUNITY_INVITE_CANCELED,

    TEAM_MEMBER_ADDED,
    TEAM_MEMBER_REMOVED,
    TEAM_MEMBER_LEFT,
    TEAM_MEMBER_EDITED,

    NEW_POST,
    POST_COMMENT,
    POST_REACTION,

    COMMENT_REACTION,

    NEW_MESSAGE,
    // Primeira mensagem de uma conversa DIRECT que nasceu PENDING: é uma solicitação
    // pra trocar mensagens, não uma mensagem comum, e a aba de notificações separa as duas.
    MESSAGE_REQUEST,

    POST_MENTION,
    COMMENT_MENTION,
    TEAM_MENTION,
    COMMUNITY_MENTION,

    SYSTEM
}
