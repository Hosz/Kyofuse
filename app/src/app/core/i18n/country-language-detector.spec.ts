import { describe, expect, it } from 'vitest';
import { detectLanguageFromCountry } from './country-language-detector';

describe('country-language-detector', () => {
  it('detecta português para Brasil e Portugal por nome e sigla', () => {
    expect(detectLanguageFromCountry('BR')).toBe('pt');
    expect(detectLanguageFromCountry('brasil')).toBe('pt');
    expect(detectLanguageFromCountry('Brazil')).toBe('pt');
    expect(detectLanguageFromCountry('PT')).toBe('pt');
    expect(detectLanguageFromCountry('Portugal')).toBe('pt');
  });

  it('detecta espanhol para países hispanofalantes', () => {
    expect(detectLanguageFromCountry('AR')).toBe('es');
    expect(detectLanguageFromCountry('Argentina')).toBe('es');
    expect(detectLanguageFromCountry('ES')).toBe('es');
    expect(detectLanguageFromCountry('Espanha')).toBe('es');
    expect(detectLanguageFromCountry('Chile')).toBe('es');
  });

  it('detecta inglês para US, UK, Canada, Australia', () => {
    expect(detectLanguageFromCountry('US')).toBe('en');
    expect(detectLanguageFromCountry('USA')).toBe('en');
    expect(detectLanguageFromCountry('United States')).toBe('en');
    expect(detectLanguageFromCountry('GB')).toBe('en');
  });

  it('detecta francês, alemão, russo, chinês e japonês', () => {
    expect(detectLanguageFromCountry('FR')).toBe('fr');
    expect(detectLanguageFromCountry('France')).toBe('fr');
    expect(detectLanguageFromCountry('DE')).toBe('de');
    expect(detectLanguageFromCountry('Germany')).toBe('de');
    expect(detectLanguageFromCountry('RU')).toBe('ru');
    expect(detectLanguageFromCountry('Russia')).toBe('ru');
    expect(detectLanguageFromCountry('CN')).toBe('zh');
    expect(detectLanguageFromCountry('China')).toBe('zh');
    expect(detectLanguageFromCountry('JP')).toBe('ja');
    expect(detectLanguageFromCountry('Japan')).toBe('ja');
  });

  it('retorna null para países não mapeados ou valores vazios', () => {
    expect(detectLanguageFromCountry('')).toBeNull();
    expect(detectLanguageFromCountry(null)).toBeNull();
    expect(detectLanguageFromCountry(undefined)).toBeNull();
    expect(detectLanguageFromCountry('Atlântida')).toBeNull();
  });
});
