export interface ChatParticipant {
  name: string;
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

export interface ChatMessage {
  id: string;
  author: 'me' | 'them';
  content: string;
  timestamp: string;
  read?: boolean;
}

export interface Conversation {
  id: string;
  participant: ChatParticipant;
  relationship: MessageRelationship;
  lastMessageAt: string;
  unread?: boolean;
  isTyping?: boolean;
  messages: ChatMessage[];
}
