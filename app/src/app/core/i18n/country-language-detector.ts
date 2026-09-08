import { AppLanguage } from './i18n.types';

const COUNTRY_TO_LANGUAGE_MAP: Record<string, AppLanguage> = {
  // Português
  br: 'pt',
  brasil: 'pt',
  brazil: 'pt',
  pt: 'pt',
  portugal: 'pt',
  ao: 'pt',
  angola: 'pt',
  mz: 'pt',
  moçambique: 'pt',
  mozambique: 'pt',
  cv: 'pt',
  'cabo verde': 'pt',
  gw: 'pt',
  'guiné-bissau': 'pt',
  st: 'pt',
  'são tomé e príncipe': 'pt',
  tl: 'pt',
  'timor-leste': 'pt',

  // Español
  es: 'es',
  españa: 'es',
  espanha: 'es',
  spain: 'es',
  ar: 'es',
  argentina: 'es',
  cl: 'es',
  chile: 'es',
  co: 'es',
  colômbia: 'es',
  colombia: 'es',
  mx: 'es',
  méxico: 'es',
  mexico: 'es',
  pe: 'es',
  peru: 'es',
  uy: 'es',
  uruguai: 'es',
  uruguay: 'es',
  ve: 'es',
  venezuela: 'es',
  bo: 'es',
  bolívia: 'es',
  bolivia: 'es',
  ec: 'es',
  equador: 'es',
  ecuador: 'es',
  py: 'es',
  paraguai: 'es',
  paraguay: 'es',
  cr: 'es',
  'costa rica': 'es',
  cu: 'es',
  cuba: 'es',
  do: 'es',
  'república dominicana': 'es',
  gt: 'es',
  guatemala: 'es',
  hn: 'es',
  honduras: 'es',
  ni: 'es',
  nicarágua: 'es',
  nicaragua: 'es',
  pa: 'es',
  panamá: 'es',
  panama: 'es',
  sv: 'es',
  'el salvador': 'es',

  // Français
  fr: 'fr',
  frança: 'fr',
  france: 'fr',
  be: 'fr',
  bélgica: 'fr',
  belgium: 'fr',
  mc: 'fr',
  mônaco: 'fr',
  monaco: 'fr',
  lu: 'fr',
  luxemburgo: 'fr',
  luxembourg: 'fr',
  sn: 'fr',
  senegal: 'fr',
  ci: 'fr',
  'costa do marfim': 'fr',
  cm: 'fr',
  camarões: 'fr',
  cameroon: 'fr',

  // Deutsch
  de: 'de',
  alemanha: 'de',
  germany: 'de',
  deutschland: 'de',
  at: 'de',
  áustria: 'de',
  austria: 'de',
  ch: 'de', // Suíça (predominantemente alemão)
  suíça: 'de',
  switzerland: 'de',
  li: 'de',
  liechtenstein: 'de',

  // Русский
  ru: 'ru',
  rússia: 'ru',
  russia: 'ru',
  by: 'ru',
  bielorrússia: 'ru',
  belarus: 'ru',
  kz: 'ru',
  cazaquistão: 'ru',
  kazakhstan: 'ru',
  kg: 'ru',
  quirguistão: 'ru',
  kyrgyzstan: 'ru',
  uz: 'ru',
  uzbequistão: 'ru',
  uzbekistan: 'ru',
  am: 'ru',
  armênia: 'ru',
  az: 'ru',
  azerbaijão: 'ru',

  // 简体中文
  cn: 'zh',
  china: 'zh',
  tw: 'zh',
  taiwan: 'zh',
  hk: 'zh',
  'hong kong': 'zh',
  mo: 'zh',
  macau: 'zh',
  sg: 'zh',
  singapura: 'zh',
  singapore: 'zh',

  // 日本語
  jp: 'ja',
  japão: 'ja',
  japan: 'ja',

  // English
  us: 'en',
  usa: 'en',
  'estados unidos': 'en',
  'united states': 'en',
  gb: 'en',
  uk: 'en',
  'reino unido': 'en',
  'united kingdom': 'en',
  ca: 'en',
  canadá: 'en',
  canada: 'en',
  au: 'en',
  austrália: 'en',
  australia: 'en',
  nz: 'en',
  'nova zelândia': 'en',
  'new zealand': 'en',
  ie: 'en',
  irlanda: 'en',
  ireland: 'en',
  za: 'en',
  'áfrica do sul': 'en',
  'south africa': 'en',
  in: 'en',
  índia: 'en',
  india: 'en',
};

export function detectLanguageFromCountry(country: string | null | undefined): AppLanguage | null {
  if (!country) return null;
  const normalized = country.trim().toLowerCase();
  if (COUNTRY_TO_LANGUAGE_MAP[normalized]) {
    return COUNTRY_TO_LANGUAGE_MAP[normalized];
  }
  const withoutAccents = normalized.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  return COUNTRY_TO_LANGUAGE_MAP[withoutAccents] ?? null;
}

export function detectLanguageFromBrowser(): AppLanguage {
  if (typeof navigator === 'undefined' || !navigator.language) {
    return 'pt';
  }

  const primary = navigator.language.toLowerCase();

  if (primary.startsWith('pt')) return 'pt';
  if (primary.startsWith('es')) return 'es';
  if (primary.startsWith('fr')) return 'fr';
  if (primary.startsWith('de')) return 'de';
  if (primary.startsWith('ru')) return 'ru';
  if (primary.startsWith('zh')) return 'zh';
  if (primary.startsWith('ja')) return 'ja';
  if (primary.startsWith('en')) return 'en';

  // Check language list if available
  if (navigator.languages && navigator.languages.length) {
    for (const lang of navigator.languages) {
      const l = lang.toLowerCase();
      if (l.startsWith('pt')) return 'pt';
      if (l.startsWith('es')) return 'es';
      if (l.startsWith('fr')) return 'fr';
      if (l.startsWith('de')) return 'de';
      if (l.startsWith('ru')) return 'ru';
      if (l.startsWith('zh')) return 'zh';
      if (l.startsWith('ja')) return 'ja';
      if (l.startsWith('en')) return 'en';
    }
  }

  return 'pt';
}
