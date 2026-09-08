import { Injectable, signal, computed, inject, PLATFORM_ID, effect } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AppLanguage, SUPPORTED_LANGUAGES, TranslationDictionary } from './i18n.types';
import { TRANSLATIONS } from './translations';
import { detectLanguageFromBrowser, detectLanguageFromCountry } from './country-language-detector';

@Injectable({
  providedIn: 'root',
})
export class I18nService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly STORAGE_KEY = 'kyofuse_language';
  private readonly MANUAL_KEY = 'kyofuse_language_manual';

  readonly supportedLanguages = SUPPORTED_LANGUAGES;

  readonly currentLang = signal<AppLanguage>(this.getInitialLanguage());

  readonly currentLangInfo = computed(
    () => this.supportedLanguages.find((l) => l.code === this.currentLang()) || this.supportedLanguages[0]
  );

  readonly dictionary = computed<TranslationDictionary>(() => TRANSLATIONS[this.currentLang()]);

  constructor() {
    this.updateHtmlLang(this.currentLang());

    effect(() => {
      const lang = this.currentLang();
      this.updateHtmlLang(lang);
    });
  }

  public setLanguage(lang: AppLanguage, manual = true): void {
    if (!TRANSLATIONS[lang]) return;

    this.currentLang.set(lang);

    if (this.isBrowser) {
      try {
        localStorage.setItem(this.STORAGE_KEY, lang);
        if (manual) {
          localStorage.setItem(this.MANUAL_KEY, 'true');
        }
      } catch (e) {
        console.warn('Failed to save language in localStorage:', e);
      }
    }
  }

  /**
   * Chamado quando o perfil do usuário é carregado ou no login/registro.
   * Se o usuário ainda não escolheu um idioma manualmente, auto-seleciona
   * pelo país de criação da conta.
   */
  public initFromCountry(country: string | null | undefined): void {
    if (!this.isBrowser) return;

    const isManual = localStorage.getItem(this.MANUAL_KEY) === 'true';
    if (isManual) {
      // O usuário já escolheu seu idioma nas configurações; respeita a preferência dele.
      return;
    }

    const detected = detectLanguageFromCountry(country);
    if (detected && detected !== this.currentLang()) {
      this.setLanguage(detected, false);
    }
  }

  public t(key: string, params?: Record<string, string | number>): string {
    const dict = this.dictionary() as any;
    const parts = key.split('.');
    let value: any = dict;

    for (const part of parts) {
      if (value && typeof value === 'object' && part in value) {
        value = value[part];
      } else {
        // Fallback para português caso a chave falte
        value = this.getFallback(key);
        break;
      }
    }

    if (typeof value !== 'string') {
      return key;
    }

    if (params) {
      return Object.entries(params).reduce(
        (acc, [k, v]) => acc.replace(new RegExp(`\\{${k}\\}`, 'g'), String(v)),
        value
      );
    }

    return value;
  }

  private getFallback(key: string): string {
    const ptDict = TRANSLATIONS.pt as any;
    const parts = key.split('.');
    let value: any = ptDict;
    for (const part of parts) {
      if (value && typeof value === 'object' && part in value) {
        value = value[part];
      } else {
        return key;
      }
    }
    return typeof value === 'string' ? value : key;
  }

  private getInitialLanguage(): AppLanguage {
    if (!this.isBrowser) return 'pt';

    try {
      const saved = localStorage.getItem(this.STORAGE_KEY) as AppLanguage | null;
      if (saved && TRANSLATIONS[saved]) {
        return saved;
      }
    } catch {
      // Ignore
    }

    return detectLanguageFromBrowser();
  }

  private updateHtmlLang(lang: AppLanguage): void {
    if (this.isBrowser && document.documentElement) {
      document.documentElement.lang = lang;
    }
  }
}
