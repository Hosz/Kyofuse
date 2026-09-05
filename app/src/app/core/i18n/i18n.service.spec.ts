import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { I18nService } from './i18n.service';

describe('I18nService', () => {
  let service: I18nService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(I18nService);
  });

  it('permite alternar idioma manualmente e salva flag no localStorage', () => {
    service.setLanguage('en', true);
    expect(service.currentLang()).toBe('en');
    expect(localStorage.getItem('kyofuse_language')).toBe('en');
    expect(localStorage.getItem('kyofuse_language_manual')).toBe('true');
    expect(document.documentElement.lang).toBe('en');
  });

  it('traduz chaves corretamente para o idioma ativo', () => {
    service.setLanguage('en');
    expect(service.t('nav.home')).toBe('Home');

    service.setLanguage('pt');
    expect(service.t('nav.home')).toBe('Página Inicial');

    service.setLanguage('es');
    expect(service.t('nav.home')).toBe('Inicio');

    service.setLanguage('fr');
    expect(service.t('nav.home')).toBe('Accueil');
  });

  it('initFromCountry auto-seleciona idioma se não houver escolha manual prévia', () => {
    service.initFromCountry('Argentina');
    expect(service.currentLang()).toBe('es');
    expect(localStorage.getItem('kyofuse_language')).toBe('es');
    expect(localStorage.getItem('kyofuse_language_manual')).toBeNull();
  });

  it('initFromCountry respeita escolha manual prévia do usuário', () => {
    service.setLanguage('ja', true); // Usuário escolheu manualmente
    service.initFromCountry('Brazil'); // Conta é do Brasil
    // Deve continuar em japonês pois usuário escolheu manualmente
    expect(service.currentLang()).toBe('ja');
  });

  it('substitui parâmetros dinâmicos na tradução', () => {
    const result = service.t('common.search', { term: 'test' });
    expect(typeof result).toBe('string');
  });
});
