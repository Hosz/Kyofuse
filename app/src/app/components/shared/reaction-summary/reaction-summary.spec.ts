import { describe, expect, it } from 'vitest';
import { buildReactionLabel } from './reaction-summary';

describe('buildReactionLabel', () => {
  it('usa o singular quando só uma pessoa reagiu', () => {
    expect(buildReactionLabel(1, ['Ana'])).toBe('Ana reagiu');
  });

  it('liga os nomes com "e" quando não sobra ninguém', () => {
    expect(buildReactionLabel(2, ['Ana', 'Bia'])).toBe('Ana e Bia reagiram');
  });

  it('usa vírgula entre os nomes quando existe "e mais N"', () => {
    // "Ana e Bia e mais 13" teria dois "e" seguidos.
    expect(buildReactionLabel(15, ['Ana', 'Bia'])).toBe('Ana, Bia e mais 13 pessoas reagiram');
  });

  it('usa o singular no resto quando sobra exatamente uma pessoa', () => {
    expect(buildReactionLabel(3, ['Ana', 'Bia'])).toBe('Ana, Bia e mais 1 pessoa reagiram');
  });

  it('mostra só a contagem enquanto os nomes não chegaram', () => {
    expect(buildReactionLabel(7, [])).toBe('7 pessoas reagiram');
    expect(buildReactionLabel(1, [])).toBe('1 pessoa reagiu');
  });

  it('concorda o verbo com os nomes citados se vierem mais nomes que o total', () => {
    // O total vem do post e os nomes de outra requisição; podem divergir por um instante.
    expect(buildReactionLabel(1, ['Ana', 'Bia'])).toBe('Ana e Bia reagiram');
  });
});
