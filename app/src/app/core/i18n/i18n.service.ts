import { Injectable, signal, computed, inject, PLATFORM_ID, effect } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '../../models/api-url.model';
import { AppLanguage, SUPPORTED_LANGUAGES, TranslationDictionary } from './i18n.types';
import { TRANSLATIONS } from './translations';
import { detectLanguageFromBrowser, detectLanguageFromCountry, detectLanguageFromDevice } from './country-language-detector';

@Injectable({
  providedIn: 'root',
})
export class I18nService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly http = inject(HttpClient, { optional: true });
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

    this.initFromDeviceLocation();
  }

  public setLanguage(lang: AppLanguage, manual = true): void {
    if (!TRANSLATIONS[lang]) return;

    this.currentLang.set(lang);
    this.updateHtmlLang(lang);

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

  /**
   * Inicializa o idioma a partir da localização do dispositivo onde o acesso/login está ocorrendo.
   * Se o usuário ainda não escolheu um idioma manualmente nas configurações,
   * detecta o idioma físico do dispositivo (fuso horário + idioma do navegador)
   * e refina opcionalmente com o país do IP retornado pelo backend.
   */
  public initFromDeviceLocation(): void {
    if (!this.isBrowser) return;

    const isManual = localStorage.getItem(this.MANUAL_KEY) === 'true';
    if (isManual) return;

    const deviceLang = detectLanguageFromDevice();
    if (deviceLang && deviceLang !== this.currentLang()) {
      this.setLanguage(deviceLang, false);
    }

    if (this.http) {
      this.http.get<{ countryCode?: string }>(`${API_URL}/api/auth/location`).subscribe({
        next: (res) => {
          if (res?.countryCode && localStorage.getItem(this.MANUAL_KEY) !== 'true') {
            const detected = detectLanguageFromCountry(res.countryCode);
            if (detected && detected !== this.currentLang()) {
              this.setLanguage(detected, false);
            }
          }
        },
        error: () => {
          // Ignora falhas de rede/offline
        },
      });
    }
  }

  private getInitialLanguage(): AppLanguage {
    if (!this.isBrowser) return 'pt';

    try {
      const isManual = localStorage.getItem(this.MANUAL_KEY) === 'true';
      const saved = localStorage.getItem(this.STORAGE_KEY) as AppLanguage | null;
      if (isManual && saved && TRANSLATIONS[saved]) {
        return saved;
      }
    } catch {
      // Ignore
    }

    return detectLanguageFromDevice();
  }

  private updateHtmlLang(lang: AppLanguage): void {
    if (this.isBrowser && document.documentElement) {
      document.documentElement.lang = lang;
    }
  }
}
