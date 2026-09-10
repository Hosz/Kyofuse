import { describe, expect, it } from 'vitest';
import {
  isLongChatMessage,
  truncateChatMessage,
  CHAT_MESSAGE_MAX_CHARS,
  CHAT_MESSAGE_MAX_LINES,
} from './format.util';

describe('format.util - chat message truncation', () => {
  describe('isLongChatMessage', () => {
    it('should return false for null, undefined, or empty string', () => {
      expect(isLongChatMessage(null)).toBe(false);
      expect(isLongChatMessage(undefined)).toBe(false);
      expect(isLongChatMessage('')).toBe(false);
    });

    it('should return false for short single-line messages', () => {
      expect(isLongChatMessage('Olá, tudo bem?')).toBe(false);
    });

    it('should return false for messages within character and line limits', () => {
      const text = 'Linha 1\nLinha 2\nLinha 3\nLinha 4\nLinha 5';
      expect(isLongChatMessage(text)).toBe(false);
    });

    it('should return true when text exceeds character limit', () => {
      const longText = 'a'.repeat(CHAT_MESSAGE_MAX_CHARS + 1);
      expect(isLongChatMessage(longText)).toBe(true);
    });

    it('should return true when text exceeds line limit', () => {
      const multilineText = Array(CHAT_MESSAGE_MAX_LINES + 2).fill('linha').join('\n');
      expect(isLongChatMessage(multilineText)).toBe(true);
    });

    it('should respect custom thresholds', () => {
      expect(isLongChatMessage('1234567890', 5, 2)).toBe(true);
      expect(isLongChatMessage('1\n2\n3', 50, 2)).toBe(true);
      expect(isLongChatMessage('123', 5, 2)).toBe(false);
    });
  });

  describe('truncateChatMessage', () => {
    it('should return empty string for null or undefined', () => {
      expect(truncateChatMessage(null)).toBe('');
      expect(truncateChatMessage(undefined)).toBe('');
      expect(truncateChatMessage('')).toBe('');
    });

    it('should return untouched string for short messages', () => {
      const msg = 'Mensagem curta de teste';
      expect(truncateChatMessage(msg)).toBe(msg);
    });

    it('should truncate at word boundaries for long text', () => {
      const words = 'palavra '.repeat(80).trim(); // ~639 chars
      const truncated = truncateChatMessage(words, 100, 10);
      expect(truncated.length).toBeLessThanOrEqual(100);
      expect(truncated.endsWith('palavra')).toBe(true);
    });

    it('should truncate by line count when lines exceed maxLines', () => {
      const lines = ['Linha 1', 'Linha 2', 'Linha 3', 'Linha 4', 'Linha 5', 'Linha 6', 'Linha 7', 'Linha 8'];
      const truncated = truncateChatMessage(lines.join('\n'), 1000, 4);
      expect(truncated).toBe('Linha 1\nLinha 2\nLinha 3\nLinha 4');
    });
  });
});
