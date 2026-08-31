export const FALLBACK_AVATAR_URL =
  'https://www.shutterstock.com/image-vector/blank-avatar-photo-place-holder-600nw-1114445501.jpg';

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

export function toTimeAgo(isoDate: string): string {
  if (!isoDate) return 'agora';
  const minutes = Math.floor((Date.now() - new Date(isoDate).getTime()) / 60000);
  if (minutes < 1) return 'agora';
  if (minutes < 60) return `${minutes}min`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h`;
  return `${Math.floor(hours / 24)}d`;
}

export function formatJoinedDate(dateStr: string | null | undefined): string | null {
  if (!dateStr) return null;
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return null;

  const monthYear = date.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  return `Entrou em ${monthYear}`;
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
export function formatMessageDayDivider(isoDate: string): string {
  if (!isoDate) return '';
  const date = new Date(isoDate);
  if (isNaN(date.getTime())) return '';

  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const target = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const diffTime = today.getTime() - target.getTime();
  const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    return 'Hoje';
  }
  if (diffDays === 1) {
    return 'Ontem';
  }
  if (diffDays >= 2 && diffDays < 7) {
    const weekday = date.toLocaleDateString('pt-BR', { weekday: 'long' });
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
