export const FALLBACK_AVATAR_URL = '/assets/profile/profile-image-default.png';
export const TEAM_FALLBACK_AVATAR_URL = '/assets/profile/team-profile-image-default.png';
export const COMMUNITY_FALLBACK_AVATAR_URL = '/assets/profile/community-profile-image-default.png';

/**
 * Deriva um slug a partir de um nome livre, no formato que o backend exige
 * (`^[a-z0-9]+(?:-[a-z0-9]+)*$`): minúsculas, sem acento, hífen único entre termos.
 * Usado na criação de Teams e Communities, onde o slug nasce sugerido a partir do
 * nome mas continua editável pelo usuário.
 */
export function toSlug(value: string): string {
  return value
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

export function toTimeAgo(isoDate: string, lang: string = 'pt'): string {
  const getNow = () => {
    switch (lang) {
      case 'en': return 'now';
      case 'es': return 'ahora';
      case 'fr': return 'maintenant';
      case 'de': return 'jetzt';
      case 'ru': return 'сейчас';
      case 'zh': return '刚刚';
      case 'ja': return 'たった今';
      default: return 'agora';
    }
  };

  if (!isoDate) return getNow();
  const minutes = Math.floor((Date.now() - new Date(isoDate).getTime()) / 60000);
  if (minutes < 1) return getNow();
  if (minutes < 60) {
    switch (lang) {
      case 'en': return `${minutes}m`;
      case 'es': return `${minutes}min`;
      case 'fr': return `${minutes}min`;
      case 'de': return `${minutes}min`;
      case 'ru': return `${minutes}мин`;
      case 'zh': return `${minutes}分钟前`;
      case 'ja': return `${minutes}分前`;
      default: return `${minutes}min`;
    }
  }
  const hours = Math.floor(minutes / 60);
  if (hours < 24) {
    switch (lang) {
      case 'zh': return `${hours}小时前`;
      case 'ja': return `${hours}時間前`;
      default: return `${hours}h`;
    }
  }
  const days = Math.floor(hours / 24);
  switch (lang) {
    case 'zh': return `${days}天前`;
    case 'ja': return `${days}日前`;
    default: return `${days}d`;
  }
}

export function formatJoinedDate(
  dateStr: string | null | undefined,
  lang: string = 'pt',
  template?: string,
): string | null {
  if (!dateStr) return null;
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return null;

  const localeMap: Record<string, string> = {
    pt: 'pt-BR',
    en: 'en-US',
    es: 'es-ES',
    fr: 'fr-FR',
    de: 'de-DE',
    ru: 'ru-RU',
    zh: 'zh-CN',
    ja: 'ja-JP',
  };
  const locale = localeMap[lang] || 'pt-BR';
  const monthYear = date.toLocaleDateString(locale, { month: 'long', year: 'numeric' });

  if (template) {
    return template.replace('{date}', monthYear);
  }

  switch (lang) {
    case 'en': return `Joined ${monthYear}`;
    case 'es': return `Se unió en ${monthYear}`;
    case 'fr': return `A rejoint en ${monthYear}`;
    case 'de': return `Beigetreten im ${monthYear}`;
    case 'ru': return `Присоединился в ${monthYear}`;
    case 'zh': return `加入于 ${monthYear}`;
    case 'ja': return `${monthYear}に参加`;
    default: return `Entrou em ${monthYear}`;
  }
}

/**
 * Retorna a data e hora formatada para exibição em detalhes do post:
 * Ex: "21:45 · 29/08/2026"
 */
export function formatFullPostDateTime(isoDate: string | undefined): string {
  if (!isoDate) return '';
  const date = new Date(isoDate);
  if (isNaN(date.getTime())) return '';

  const time = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();

  return `${time} · ${day}/${month}/${year}`;
}

/**
 * Retorna apenas hora e minuto (HH:mm) para mensagens de chat.
 * Ex: "21:45"
 */
export function toExactTime(isoDate: string): string {
  if (!isoDate) return '';
  const date = new Date(isoDate);
  if (isNaN(date.getTime())) return '';
  return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

/**
 * Retorna a data e hora completas para tooltip de hover em mensagem de chat:
 * Ex: "29/08/2026 às 21:45:03"
 */
export function toFullDateTimeTooltip(isoDate: string): string {
  if (!isoDate) return '';
  const date = new Date(isoDate);
  if (isNaN(date.getTime())) return '';
  const time = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${day}/${month}/${year} às ${time}`;
}

/**
 * Retorna uma chave única para agrupar mensagens pelo mesmo dia calendário (YYYY-MM-DD).
 */
export function getMessageDayKey(isoDate: string): string {
  if (!isoDate) return '';
  const d = new Date(isoDate);
  if (isNaN(d.getTime())) return '';
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

/**
 * Formata o divisor de dia do chat conforme a regra:
 * - "Hoje"
 * - "Ontem"
 * - Nome do dia da semana nos últimos 7 dias (ex: "Quinta-feira", "Sexta-feira")
 * - "DD/MM" se for depois da semana no mesmo ano (ex: "30/05", "15/02")
 * - "DD/MM/YY" se virar o ano (ex: "30/05/26", "15/02/25")
 */
export function formatMessageDayDivider(isoDate: string, lang: string = 'pt'): string {
  if (!isoDate) return '';
  const date = new Date(isoDate);
  if (isNaN(date.getTime())) return '';

  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const target = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const diffTime = today.getTime() - target.getTime();
  const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));

  const localeMap: Record<string, string> = {
    pt: 'pt-BR',
    en: 'en-US',
    es: 'es-ES',
    fr: 'fr-FR',
    de: 'de-DE',
    ru: 'ru-RU',
    zh: 'zh-CN',
    ja: 'ja-JP',
  };
  const locale = localeMap[lang] || 'pt-BR';

  if (diffDays === 0) {
    switch (lang) {
      case 'en': return 'Today';
      case 'es': return 'Hoy';
      case 'fr': return "Aujourd'hui";
      case 'de': return 'Heute';
      case 'ru': return 'Сегодня';
      case 'zh': return '今天';
      case 'ja': return '今日';
      default: return 'Hoje';
    }
  }
  if (diffDays === 1) {
    switch (lang) {
      case 'en': return 'Yesterday';
      case 'es': return 'Ayer';
      case 'fr': return 'Hier';
      case 'de': return 'Gestern';
      case 'ru': return 'Вчера';
      case 'zh': return '昨天';
      case 'ja': return '昨日';
      default: return 'Ontem';
    }
  }
  if (diffDays >= 2 && diffDays < 7) {
    const weekday = date.toLocaleDateString(locale, { weekday: 'long' });
    return weekday.charAt(0).toUpperCase() + weekday.slice(1);
  }

  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');

  if (date.getFullYear() === now.getFullYear()) {
    return `${day}/${month}`;
  }

  const yearShort = String(date.getFullYear()).slice(-2);
  return `${day}/${month}/${yearShort}`;
}

export const CHAT_MESSAGE_MAX_CHARS = 400;
export const CHAT_MESSAGE_MAX_LINES = 6;

/**
 * Retorna se uma mensagem de chat é considerada longa o bastante para ser truncada
 * no fluxo de mensagens e exigir a abertura do modal de visualização completa ("Ver mais").
 */
export function isLongChatMessage(
  text: string | null | undefined,
  maxChars = CHAT_MESSAGE_MAX_CHARS,
  maxLines = CHAT_MESSAGE_MAX_LINES,
): boolean {
  if (!text) return false;
  if (text.length > maxChars) return true;
  const lines = text.split('\n');
  return lines.length > maxLines;
}

/**
 * Trunca o texto de uma mensagem de chat de forma limpa, respeitando limites de
 * caracteres e quebras de linha, cortando no último espaço dentro de uma janela razoável.
 */
export function truncateChatMessage(
  text: string | null | undefined,
  maxChars = CHAT_MESSAGE_MAX_CHARS,
  maxLines = CHAT_MESSAGE_MAX_LINES,
): string {
  if (!text) return '';
  const lines = text.split('\n');
  const isLongByLines = lines.length > maxLines;
  const isLongByChars = text.length > maxChars;

  if (!isLongByLines && !isLongByChars) {
    return text;
  }

  let cutIndex = maxChars;
  let cutByLines = false;

  if (isLongByLines) {
    const lineSlice = lines.slice(0, maxLines).join('\n');
    if (lineSlice.length < cutIndex) {
      cutIndex = lineSlice.length;
      cutByLines = true;
    }
  }

  if (cutIndex > text.length) {
    cutIndex = text.length;
  }

  // Corta no último espaço se estiver cortando no meio de uma palavra
  if (!cutByLines) {
    const nextChar = text.charAt(cutIndex);
    if (nextChar !== ' ' && nextChar !== '\n') {
      const lastSpace = text.lastIndexOf(' ', cutIndex);
      if (lastSpace > cutIndex - 40 && lastSpace > 0) {
        cutIndex = lastSpace;
      }
    }
  }

  return text.slice(0, cutIndex).trimEnd();
}
