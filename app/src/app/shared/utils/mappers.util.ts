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
    },
  };
}

/**
 * Cada tipo de conversa tira sua identidade visual de um lugar: DIRECT usa o perfil do
 * outro participante, GROUP tem nome e foto próprios e COMMUNITY herda os da comunidade
 * vinculada.
 */
export function toConversation(conversation: ConversationResponse, myUserId: string): Conversation {
  if (conversation.type === 'DIRECT') {
    const iAmUserOne = conversation.directUserOneId === myUserId;
    const otherId = (iAmUserOne ? conversation.directUserTwoId : conversation.directUserOneId) ?? '';
    const otherUsername = (iAmUserOne ? conversation.directUserTwoUsername : conversation.directUserOneUsername) ?? '';
    const otherNickname = iAmUserOne ? conversation.directUserTwoNickname : conversation.directUserOneNickname;
    const otherAvatarUrl = iAmUserOne ? conversation.directUserTwoAvatarUrl : conversation.directUserOneAvatarUrl;
    const isCreator = conversation.createdById === myUserId;

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
      relationship: toRelationship(conversation.directMessageStatus, isCreator),
      lastMessageAt: toTimeAgo(conversation.updatedAt),
      messages: [],
    };
  }

  const isCommunity = conversation.type === 'COMMUNITY';
  const name = (isCommunity ? conversation.communityName : conversation.name) ?? (isCommunity ? 'Comunidade' : 'Grupo');
  // GROUP tem foto própria (conversations.avatar_url); COMMUNITY exibe a da comunidade
  // vinculada, que vem junto da conversa.
  const avatarUrl = (isCommunity ? conversation.communityAvatarUrl : conversation.avatarUrl) || FALLBACK_AVATAR_URL;

  return {
    id: conversation.id,
    type: conversation.type,
    participant: {
      name,
      handle: isCommunity ? 'comunidade' : 'grupo',
      avatarUrl,
    },
    relationship: 'mutual',
    lastMessageAt: toTimeAgo(conversation.updatedAt),
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
    senderUsername: message.senderUsername,
    senderNickname: message.senderNickname ?? undefined,
    senderAvatarUrl: message.senderAvatarUrl ?? undefined,
  };
}

/** Janela em que mensagens seguidas do mesmo autor são consideradas uma rajada só. */
const MESSAGE_GROUP_WINDOW_MS = 5 * 60 * 1000;

export function toChatMessageGroups(messages: ChatMessage[]): ChatMessageGroup[] {
  const groups: ChatMessageGroup[] = [];
  let lastDayKey = '';

  for (const message of messages) {
    const current = groups.at(-1);
    const previous = current?.messages.at(-1);
    const messageDayKey = getMessageDayKey(message.createdAt);
    const isNewDay = messageDayKey !== lastDayKey;

    const sameAuthor =
      current?.author === message.author && current?.senderUsername === message.senderUsername;
    const withinWindow =
      !isNewDay &&
      !!previous &&
      new Date(message.createdAt).getTime() - new Date(previous.createdAt).getTime() <= MESSAGE_GROUP_WINDOW_MS;

    if (current && sameAuthor && withinWindow) {
      current.messages.push(message);
      // O bloco mostra o horário da mensagem mais recente dele.
      current.timestamp = message.timestamp;
      current.timeFormatted = message.exactTime;
      continue;
    }

    const dayDivider = isNewDay ? formatMessageDayDivider(message.createdAt) : undefined;
    if (isNewDay) {
      lastDayKey = messageDayKey;
    }

    groups.push({
      key: message.id,
      dayDivider,
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
