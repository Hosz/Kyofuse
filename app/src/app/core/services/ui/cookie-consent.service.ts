import { Injectable, signal } from '@angular/core';

export interface CookieConsentPreferences {
  essential: true;
  functional: boolean;
  analytics: boolean;
  decidedAt: string;
  version: string;
}

export const DEFAULT_COOKIE_PREFERENCES: CookieConsentPreferences = {
  essential: true,
  functional: true,
  analytics: false,
  decidedAt: '',
  version: '1.0',
};

export const COOKIE_CONSENT_STORAGE_KEY = 'kyofuse_cookie_consent_v1';

@Injectable({
  providedIn: 'root',
})
export class CookieConsentService {
  readonly preferences = signal<CookieConsentPreferences>({ ...DEFAULT_COOKIE_PREFERENCES });
  readonly hasConsent = signal<boolean>(false);
  readonly bannerVisible = signal<boolean>(false);
  readonly modalVisible = signal<boolean>(false);

  constructor() {
    this.initConsent();
  }

  private initConsent(): void {
    try {
      if (typeof window === 'undefined' || typeof localStorage === 'undefined') {
        return;
      }

      const stored = localStorage.getItem(COOKIE_CONSENT_STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored) as CookieConsentPreferences;
        if (parsed && typeof parsed === 'object' && parsed.essential === true) {
          this.preferences.set(parsed);
          this.hasConsent.set(true);
          this.bannerVisible.set(false);
          return;
        }
      }

      // Se ainda não consentiu, exibe o banner após uma pequena espera
      setTimeout(() => {
        if (!this.hasConsent()) {
          this.bannerVisible.set(true);
        }
      }, 600);
    } catch {
      // Ignora falhas de leitura do localStorage (ex: navegação privada restritiva)
    }
  }

  acceptAll(): void {
    const updated: CookieConsentPreferences = {
      essential: true,
      functional: true,
      analytics: true,
      decidedAt: new Date().toISOString(),
      version: '1.0',
    };
    this.save(updated);
  }

  acceptEssentialOnly(): void {
    const updated: CookieConsentPreferences = {
      essential: true,
      functional: false,
      analytics: false,
      decidedAt: new Date().toISOString(),
      version: '1.0',
    };
    this.save(updated);
  }

  saveCustomPreferences(functional: boolean, analytics: boolean): void {
    const updated: CookieConsentPreferences = {
      essential: true,
      functional,
      analytics,
      decidedAt: new Date().toISOString(),
      version: '1.0',
    };
    this.save(updated);
  }

  openPreferences(): void {
    this.modalVisible.set(true);
  }

  closePreferences(): void {
    this.modalVisible.set(false);
  }

  resetConsent(): void {
    try {
      localStorage.removeItem(COOKIE_CONSENT_STORAGE_KEY);
    } catch {
      // ignore
    }
    this.hasConsent.set(false);
    this.bannerVisible.set(true);
    this.modalVisible.set(false);
  }

  private save(prefs: CookieConsentPreferences): void {
    this.preferences.set(prefs);
    this.hasConsent.set(true);
    this.bannerVisible.set(false);
    this.modalVisible.set(false);

    try {
      localStorage.setItem(COOKIE_CONSENT_STORAGE_KEY, JSON.stringify(prefs));
    } catch (e) {
      console.warn('Falha ao salvar preferências de cookies no localStorage:', e);
    }
  }
}
