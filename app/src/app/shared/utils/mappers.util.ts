import { Comment, Post } from '../models/social.model';
import { postResponse } from '../../models/posts/posts-response.model';
import { CommentResponse } from '../../models/comments/comments.model';
import { ChatMessage, ChatMessageGroup, Conversation, MessageRelationship } from '../models/chat.model';
import { ConversationResponse, MessageResponse } from '../../models/chat/chat.model';
import {
  FALLBACK_AVATAR_URL,
  formatMessageDayDivider,
  getMessageDayKey,
  toExactTime,
  toFullDateTimeTooltip,
  toTimeAgo,
} from './format.util';

export function toPost(post: postResponse): Post {
  const firstMedia = post.media && post.media.length > 0 ? post.media[0] : undefined;
  return {
    id: post.id,
    author: {
      id: post.authorId,
      name: post.authorNickname,
      handle: post.authorUsername,
      avatarUrl: post.authorAvatarUrl || FALLBACK_AVATAR_URL,
    },
    timeAgo: toTimeAgo(post.createdAt),
    createdAt: post.createdAt,
    content: post.content,
    media: firstMedia ? { imageUrl: firstMedia.url } : undefined,
    mediaList: post.media || [],
    stats: {
      comments: post.commentCount,
      reposts: 0,
      likes: post.likeCount,
      reactions: post.reactionCount,
      views: post.viewCount ?? 0,
    },
  };
}

/**
 * Formata o preview da última mensagem para a lista de conversas:
 * - Trunca textos longos adicionando "..."
 * - Converte mídias para etiquetas descritivas como "[Imagem]", "[Vídeo]", "[GIF]", etc.
 */
export function formatMessagePreview(
  content?: string | null,
  hasMedia?: boolean | null,
  mediaType?: string | null,
  senderName?: string | null,
  isGroupOrCommunity?: boolean,
  isMe?: boolean,
): string {
  let mediaLabel = '';
  if (hasMedia) {
    if (mediaType?.includes('gif') || mediaType?.includes('image/gif')) {
      mediaLabel = 'GIF';
    } else if (mediaType?.startsWith('video/')) {
      mediaLabel = 'Vídeo';
    } else if (mediaType?.startsWith('audio/')) {
      mediaLabel = 'Áudio';
    } else if (mediaType?.startsWith('image/')) {
      mediaLabel = 'Imagem';
    } else {
      mediaLabel = 'Arquivo';
    }
  }

  let text = '';
  const trimmedContent = content?.trim() ?? '';

  if (mediaLabel && trimmedContent) {
    text = `[${mediaLabel}] ${trimmedContent}`;
  } else if (mediaLabel) {
    text = `[${mediaLabel}]`;
  } else if (trimmedContent) {
    text = trimmedContent;
  }

  if (!text) {
    return '';
  }

  const maxLength = 45;
  if (text.length > maxLength) {
    text = text.slice(0, maxLength).trimEnd() + '...';
  }

  if (isMe) {
    return `Você: ${text}`;
  } else if (isGroupOrCommunity && senderName) {
    return `${senderName}: ${text}`;
  }

  return text;
}

export function previewFromChatMessage(message: ChatMessage, isGroupOrCommunity = false): string {
  const hasMedia = !!message.media && message.media.length > 0;
  const mediaType = hasMedia ? message.media![0].contentType : null;
  const senderName = message.senderNickname || message.senderUsername;
  const isMe = message.author === 'me';
  return formatMessagePreview(message.content, hasMedia, mediaType, senderName, isGroupOrCommunity, isMe);
}

/**
 * Cada tipo de conversa tira sua identidade visual de um lugar: DIRECT usa o perfil do
 * outro participante, GROUP tem nome e foto próprios e COMMUNITY herda os da comunidade
 * vinculada.
 */
export function toConversation(conversation: ConversationResponse, myUserId: string, lang: string = 'pt'): Conversation {
  const unreadCount = conversation.unreadCount ?? 0;
  const lastTime = conversation.lastMessageCreatedAt || conversation.updatedAt;

  if (conversation.type === 'DIRECT') {
    const iAmUserOne = conversation.directUserOneId === myUserId;
    const otherId = (iAmUserOne ? conversation.directUserTwoId : conversation.directUserOneId) ?? '';
    const otherUsername = (iAmUserOne ? conversation.directUserTwoUsername : conversation.directUserOneUsername) ?? '';
    const otherNickname = iAmUserOne ? conversation.directUserTwoNickname : conversation.directUserOneNickname;
    const otherAvatarUrl = iAmUserOne ? conversation.directUserTwoAvatarUrl : conversation.directUserOneAvatarUrl;
    const isCreator = conversation.createdById === myUserId;
    const relationship = toRelationship(conversation.directMessageStatus, isCreator);

    const isMe = iAmUserOne
      ? conversation.lastMessageSenderUsername === conversation.directUserOneUsername
      : conversation.lastMessageSenderUsername === conversation.directUserTwoUsername;

    const senderName = isMe ? 'Você' : (otherNickname || otherUsername);
    let preview = formatMessagePreview(
      conversation.lastMessageContent,
      conversation.lastMessageHasMedia,
      conversation.lastMessageMediaType,
      senderName,
      false,
      isMe,
    );

    if (!preview) {
      if (relationship === 'request-received') preview = 'Quer trocar mensagens com você';
      else if (relationship === 'request-sent') preview = 'Solicitação enviada';
      else if (relationship === 'declined') preview = 'Conversa encerrada';
      else preview = 'Toque para conversar';
    }

    return {
      id: conversation.id,
      type: 'DIRECT',
      participant: {
        id: otherId,
        // nickname é o nome de exibição; o username fica no handle, mostrado como @.
        name: otherNickname || otherUsername,
        handle: otherUsername,
        avatarUrl: otherAvatarUrl || FALLBACK_AVATAR_URL,
      },
      relationship,
      lastMessageAt: toTimeAgo(lastTime, lang),
      lastMessagePreview: preview,
      unread: unreadCount > 0 || relationship === 'request-received',
      unreadCount,
      messages: [],
      revokedById: conversation.revokedById ?? undefined,
    };
  }

  const isCommunity = conversation.type === 'COMMUNITY';
  const name = (isCommunity ? conversation.communityName : conversation.name) ?? (isCommunity ? 'Comunidade' : 'Grupo');
  // GROUP tem foto própria (conversations.avatar_url); COMMUNITY exibe a da comunidade
  // vinculada, que vem junto da conversa.
  const avatarUrl = (isCommunity ? conversation.communityAvatarUrl : conversation.avatarUrl) || FALLBACK_AVATAR_URL;

  const senderName = conversation.lastMessageSenderNickname || conversation.lastMessageSenderUsername;
  let preview = formatMessagePreview(
    conversation.lastMessageContent,
    conversation.lastMessageHasMedia,
    conversation.lastMessageMediaType,
    senderName,
    true,
    false,
  );

  if (!preview) {
    preview = isCommunity ? 'Canal da comunidade' : 'Grupo de conversa';
  }

  return {
    id: conversation.id,
    type: conversation.type,
    participant: {
      name,
      handle: isCommunity ? 'comunidade' : 'grupo',
      avatarUrl,
    },
    relationship: 'mutual',
    lastMessageAt: toTimeAgo(lastTime, lang),
    lastMessagePreview: preview,
    unread: unreadCount > 0,
    unreadCount,
    messages: [],
    communityId: isCommunity ? (conversation.communityId ?? undefined) : undefined,
  };
}

function toRelationship(status: ConversationResponse['directMessageStatus'], isCreator: boolean): MessageRelationship {
  switch (status) {
    case 'ACCEPTED':
      return 'mutual';
    case 'DECLINED':
      return 'declined';
    case 'PENDING':
      return isCreator ? 'request-sent' : 'request-received';
    default:
      return 'mutual';
  }
}

export function toChatMessage(message: MessageResponse, myUserId: string): ChatMessage {
  return {
    id: message.id,
    author: message.senderId === myUserId ? 'me' : 'them',
    content: message.content,
    media: message.media || [],
    timestamp: toTimeAgo(message.createdAt),
    exactTime: toExactTime(message.createdAt),
    tooltipTime: toFullDateTimeTooltip(message.createdAt),
    createdAt: message.createdAt,
    status: message.status || (message.senderId === myUserId ? 'SENT' : 'READ'),
    senderUsername: message.senderUsername,
    senderNickname: message.senderNickname ?? undefined,
    senderAvatarUrl: message.senderAvatarUrl ?? undefined,
  };
}

/** Janela em que mensagens seguidas do mesmo autor são consideradas uma rajada só. */
const MESSAGE_GROUP_WINDOW_MS = 5 * 60 * 1000;

export function toChatMessageGroups(
  messages: ChatMessage[],
  unreadDividerMessageId?: string | null,
  lang: string = 'pt',
  unreadLabel: string = 'Novas mensagens',
): ChatMessageGroup[] {
  const groups: ChatMessageGroup[] = [];
  const seenMessageIds = new Set<string>();
  let lastDayKey = '';

  for (const message of messages) {
    if (seenMessageIds.has(message.id)) {
      continue;
    }
    seenMessageIds.add(message.id);

    const isUnreadDivider = !!unreadDividerMessageId && unreadDividerMessageId === message.id;

    const current = groups.at(-1);
    const previous = current?.messages.at(-1);
    const messageDayKey = getMessageDayKey(message.createdAt);
    const isNewDay = messageDayKey !== lastDayKey;

    const sameAuthor =
      current?.author === message.author && current?.senderUsername === message.senderUsername;
    const withinWindow =
      !isNewDay &&
      !isUnreadDivider &&
      !!previous &&
      new Date(message.createdAt).getTime() - new Date(previous.createdAt).getTime() <= MESSAGE_GROUP_WINDOW_MS;

    if (current && sameAuthor && withinWindow) {
      current.messages.push(message);
      // O bloco mostra o horário da mensagem mais recente dele.
      current.timestamp = message.timestamp;
      current.timeFormatted = message.exactTime;
      continue;
    }

    const dayDivider = isNewDay ? formatMessageDayDivider(message.createdAt, lang) : undefined;
    if (isNewDay) {
      lastDayKey = messageDayKey;
    }

    groups.push({
      key: message.id,
      dayDivider,
      unreadDivider: isUnreadDivider ? unreadLabel : undefined,
      author: message.author,
      senderUsername: message.senderUsername,
      senderNickname: message.senderNickname,
      senderAvatarUrl: message.senderAvatarUrl,
      messages: [message],
      timeFormatted: message.exactTime,
      timestamp: message.timestamp,
    });
  }

  return groups;
}

export function toComment(comment: CommentResponse): Comment {
  return {
    id: comment.id,
    postId: comment.postId,
    authorId: comment.authorId,
    author: {
      id: comment.authorId,
      name: comment.authorNickname,
      handle: comment.authorUsername,
      avatarUrl: comment.profileImage || FALLBACK_AVATAR_URL,
    },
    timeAgo: toTimeAgo(comment.createdAt),
    content: comment.content,
    stats: {
      likes: comment.likeCount,
      reactions: comment.reactionCount,
    },
  };
}
