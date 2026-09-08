import { TestBed } from '@angular/core/testing';
import { CookieConsentService, COOKIE_CONSENT_STORAGE_KEY } from './cookie-consent.service';

describe('CookieConsentService', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('deve inicializar sem consentimento se o storage estiver vazio', () => {
    const service = TestBed.inject(CookieConsentService);
    expect(service.hasConsent()).toBe(false);
  });

  it('deve salvar consentimento ao aceitar todos os cookies', () => {
    const service = TestBed.inject(CookieConsentService);
    service.acceptAll();

    expect(service.hasConsent()).toBe(true);
    expect(service.bannerVisible()).toBe(false);
    expect(service.preferences().essential).toBe(true);
    expect(service.preferences().functional).toBe(true);
    expect(service.preferences().analytics).toBe(true);

    const stored = JSON.parse(localStorage.getItem(COOKIE_CONSENT_STORAGE_KEY) || '{}');
    expect(stored.functional).toBe(true);
    expect(stored.analytics).toBe(true);
  });

  it('deve salvar apenas essenciais ao selecionar acceptEssentialOnly', () => {
    const service = TestBed.inject(CookieConsentService);
    service.acceptEssentialOnly();

    expect(service.hasConsent()).toBe(true);
    expect(service.bannerVisible()).toBe(false);
    expect(service.preferences().essential).toBe(true);
    expect(service.preferences().functional).toBe(false);
    expect(service.preferences().analytics).toBe(false);

    const stored = JSON.parse(localStorage.getItem(COOKIE_CONSENT_STORAGE_KEY) || '{}');
    expect(stored.functional).toBe(false);
    expect(stored.analytics).toBe(false);
  });

  it('deve salvar preferências customizadas', () => {
    const service = TestBed.inject(CookieConsentService);
    service.saveCustomPreferences(true, false);

    expect(service.hasConsent()).toBe(true);
    expect(service.preferences().functional).toBe(true);
    expect(service.preferences().analytics).toBe(false);
  });

  it('deve abrir e fechar a modal de preferências', () => {
    const service = TestBed.inject(CookieConsentService);
    expect(service.modalVisible()).toBe(false);

    service.openPreferences();
    expect(service.modalVisible()).toBe(true);

    service.closePreferences();
    expect(service.modalVisible()).toBe(false);
  });

  it('deve resetar o consentimento', () => {
    const service = TestBed.inject(CookieConsentService);
    service.acceptAll();
    expect(service.hasConsent()).toBe(true);

    service.resetConsent();
    expect(service.hasConsent()).toBe(false);
    expect(service.bannerVisible()).toBe(true);
    expect(localStorage.getItem(COOKIE_CONSENT_STORAGE_KEY)).toBeNull();
  });
});
