import { AppLanguage, TranslationDictionary } from '../i18n.types';
import { pt } from './pt';
import { en } from './en';
import { es } from './es';
import { fr } from './fr';
import { de } from './de';
import { ru } from './ru';
import { zh } from './zh';
import { ja } from './ja';

export const TRANSLATIONS: Record<AppLanguage, TranslationDictionary> = {
  pt,
  en,
  es,
  fr,
  de,
  ru,
  zh,
  ja,
};
