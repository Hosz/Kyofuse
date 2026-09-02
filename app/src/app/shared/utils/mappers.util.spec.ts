import { describe, expect, it } from 'vitest';
import { toChatMessage, toChatMessageGroups, toConversation } from './mappers.util';
import { ChatMessage } from '../models/chat.model';
import { ConversationResponse, MessageResponse } from '../../models/chat/chat.model';

function message(partial: Partial<ChatMessage> & { id: string; createdAt: string }): ChatMessage {
  return {
    author: 'me',
    content: 'oi',
    timestamp: '1min',
    exactTime: '10:00',
    tooltipTime: '20/08/2026 10:00:00',
    senderUsername: 'eu',
    ...partial,
  };
}

describe('toChatMessageGroups', () => {
  it('junta mensagens seguidas do mesmo autor dentro de 5 minutos', () => {
    const groups = toChatMessageGroups([
      message({ id: '1', createdAt: '2026-08-20T10:00:00Z' }),
      message({ id: '2', createdAt: '2026-08-20T10:04:00Z' }),
    ]);

    expect(groups).toHaveLength(1);
    expect(groups[0].messages.map((m) => m.id)).toEqual(['1', '2']);
  });

  it('abre um bloco novo quando passa de 5 minutos', () => {
    const groups = toChatMessageGroups([
      message({ id: '1', createdAt: '2026-08-20T10:00:00Z' }),
      message({ id: '2', createdAt: '2026-08-20T10:06:00Z' }),
    ]);

    expect(groups).toHaveLength(2);
  });

  it('mede a janela a partir da mensagem anterior, não da primeira do bloco', () => {
    // 10:00 → 10:04 → 10:08: cada passo cabe na janela, então tudo é um bloco só.
    const groups = toChatMessageGroups([
      message({ id: '1', createdAt: '2026-08-20T10:00:00Z' }),
      message({ id: '2', createdAt: '2026-08-20T10:04:00Z' }),
      message({ id: '3', createdAt: '2026-08-20T10:08:00Z' }),
    ]);

    expect(groups).toHaveLength(1);
    expect(groups[0].messages).toHaveLength(3);
  });

  it('abre bloco novo quando o autor muda, mesmo dentro da janela', () => {
    const groups = toChatMessageGroups([
      message({ id: '1', createdAt: '2026-08-20T10:00:00Z', author: 'me', senderUsername: 'eu' }),
      message({ id: '2', createdAt: '2026-08-20T10:01:00Z', author: 'them', senderUsername: 'outro' }),
    ]);

    expect(groups).toHaveLength(2);
  });

  it('separa remetentes diferentes de um grupo, ainda que ambos sejam "them"', () => {
    const groups = toChatMessageGroups([
      message({ id: '1', createdAt: '2026-08-20T10:00:00Z', author: 'them', senderUsername: 'ana' }),
      message({ id: '2', createdAt: '2026-08-20T10:01:00Z', author: 'them', senderUsername: 'bia' }),
    ]);

    expect(groups).toHaveLength(2);
  });

  it('mostra no bloco o horário da mensagem mais recente dele', () => {
    const groups = toChatMessageGroups([
      message({ id: '1', createdAt: '2026-08-20T10:00:00Z', timestamp: '10min' }),
      message({ id: '2', createdAt: '2026-08-20T10:04:00Z', timestamp: '6min' }),
    ]);

    expect(groups[0].timestamp).toBe('6min');
  });

  it('devolve lista vazia sem mensagens', () => {
    expect(toChatMessageGroups([])).toEqual([]);
  });
});

describe('toConversation', () => {
  const base = {
    id: 'c1',
    createdById: 'me',
    updatedAt: '2026-08-20T10:00:00Z',
  } as unknown as ConversationResponse;

  it('numa DM, usa o nickname do outro como nome e o username como handle', () => {
    const conversation = toConversation(
      {
        ...base,
        type: 'DIRECT',
        directUserOneId: 'me',
        directUserTwoId: 'other',
        directUserTwoUsername: 'jogador1',
        directUserTwoNickname: 'Jogador',
        directUserTwoAvatarUrl: 'avatar.png',
        directMessageStatus: 'ACCEPTED',
      } as unknown as ConversationResponse,
      'me',
    );

    expect(conversation.participant.name).toBe('Jogador');
    expect(conversation.participant.handle).toBe('jogador1');
    expect(conversation.relationship).toBe('mutual');
  });

  it('cai pro username quando o outro não tem nickname', () => {
    const conversation = toConversation(
      {
        ...base,
        type: 'DIRECT',
        directUserOneId: 'me',
        directUserTwoId: 'other',
        directUserTwoUsername: 'jogador1',
        directUserTwoNickname: null,
        directMessageStatus: 'ACCEPTED',
      } as unknown as ConversationResponse,
      'me',
    );

    expect(conversation.participant.name).toBe('jogador1');
  });

  it('marca como pedido enviado quando eu criei uma conversa ainda pendente', () => {
    const conversation = toConversation(
      {
        ...base,
        type: 'DIRECT',
        createdById: 'me',
        directUserOneId: 'me',
        directUserTwoId: 'other',
        directUserTwoUsername: 'jogador1',
        directMessageStatus: 'PENDING',
      } as unknown as ConversationResponse,
      'me',
    );

    expect(conversation.relationship).toBe('request-sent');
  });

  it('marca como pedido recebido quando quem criou foi o outro', () => {
    const conversation = toConversation(
      {
        ...base,
        type: 'DIRECT',
        createdById: 'other',
        directUserOneId: 'me',
        directUserTwoId: 'other',
        directUserTwoUsername: 'jogador1',
        directMessageStatus: 'PENDING',
      } as unknown as ConversationResponse,
      'me',
    );

    expect(conversation.relationship).toBe('request-received');
  });

  it('numa comunidade, usa o nome e a foto da comunidade', () => {
    const conversation = toConversation(
      {
        ...base,
        type: 'COMMUNITY',
        communityId: 'com1',
        communityName: 'Time Tático',
        communityAvatarUrl: 'com.png',
      } as unknown as ConversationResponse,
      'me',
    );

    expect(conversation.participant.name).toBe('Time Tático');
    expect(conversation.participant.avatarUrl).toBe('com.png');
    expect(conversation.communityId).toBe('com1');
  });
});

describe('toChatMessage', () => {
  it('marca como minha a mensagem cujo remetente sou eu', () => {
    const mapped = toChatMessage(
      { id: 'm1', senderId: 'me', content: 'oi', createdAt: '2026-08-20T10:00:00Z' } as MessageResponse,
      'me',
    );

    expect(mapped.author).toBe('me');
  });

  it('marca como do outro quando o remetente é outra pessoa', () => {
    const mapped = toChatMessage(
      { id: 'm1', senderId: 'other', content: 'oi', createdAt: '2026-08-20T10:00:00Z' } as MessageResponse,
      'me',
    );

    expect(mapped.author).toBe('them');
  });
});
