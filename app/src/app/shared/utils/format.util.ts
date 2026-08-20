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
  const minutes = Math.floor((Date.now() - new Date(isoDate).getTime()) / 60000);
  if (minutes < 1) return 'agora';
  if (minutes < 60) return `${minutes}min`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h`;
  return `${Math.floor(hours / 24)}d`;
}
