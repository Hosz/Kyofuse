import { ConversationType } from '../../models/chat/chat.model';

export interface ChatParticipant {
  /** Id do usuário (users.id) — só preenchido em conversas DIRECT, onde participant
   * representa uma pessoa de verdade (em GROUP/COMMUNITY é o nome do grupo/comunidade). */
  id?: string;
  /** Nome de exibição: nickname do perfil em DIRECT, nome do grupo/comunidade nos outros. */
  name: string;
  /** Username sem @ — só faz sentido em DIRECT, onde o participante é uma pessoa. */
  handle: string;
  avatarUrl: string;
  badge?: string;
  badgeTone?: 'primary' | 'tertiary';
  online?: boolean;
  statusLabel?: string;
}

/**
 * Define se a troca de mensagens é livre ou precisa de aprovação:
 * - mutual: os dois se seguem (ou são amigos) — chat livre, sem avisos.
 * - request-received: o outro usuário te mandou mensagem sem relação mútua —
 *   você vê o aviso com ACEITAR/RECUSAR.
 * - request-sent: você mandou mensagem pra alguém sem relação mútua —
 *   só pode mandar uma mensagem até que seja aceita.
 * - declined: a solicitação foi recusada — conversa bloqueada.
 */
export type MessageRelationship = 'mutual' | 'request-received' | 'request-sent' | 'declined';

import { MessageMediaResponse } from '../../models/media/message-media-response.model';
import { MessageStatus } from '../../models/chat/chat.model';

export interface ChatMessage {
  id: string;
  author: 'me' | 'them';
  content: string;
  media?: MessageMediaResponse[];
  /** Texto relativo ("5min", "2h") */
  timestamp: string;
  /** Hora e minuto exatos ("21:45") */
  exactTime: string;
  /** Data e hora completas para tooltip ao passar o mouse */
  tooltipTime: string;
  /** ISO cru — usado para agrupar mensagens seguidas do mesmo autor. */
  createdAt: string;
  status?: MessageStatus;
  read?: boolean;
  senderUsername?: string;
  senderNickname?: string;
  senderAvatarUrl?: string;
}

/**
 * Mensagens seguidas do mesmo autor dentro de uma janela curta viram um bloco só, para
 * não repetir avatar e horário em cada linha de uma rajada de mensagens.
 */
export interface ChatMessageGroup {
  key: string;
  dayDivider?: string;
  unreadDivider?: string;
  author: 'me' | 'them';
  senderUsername?: string;
  senderNickname?: string;
  senderAvatarUrl?: string;
  messages: ChatMessage[];
  /** Horário do bloco: hora e minuto da última mensagem dele. */
  timeFormatted: string;
  timestamp: string;
}

export interface Conversation {
  id: string;
  type: ConversationType;
  participant: ChatParticipant;
  relationship: MessageRelationship;
  lastMessageAt: string;
  lastMessagePreview?: string;
  unread?: boolean;
  unreadCount?: number;
  isTyping?: boolean;
  messages: ChatMessage[];
  /** Só preenchido em conversas COMMUNITY — usado pra linkar de volta pra página da comunidade. */
  communityId?: string;
  /** Id do usuário que revogou a permissão da conversa (se aplicável). */
  revokedById?: string;
  hasMoreMessages?: boolean;
  messagesPage?: number;
  unreadDividerMessageId?: string | null;
}
