import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('começa sem nenhum aviso', () => {
    expect(service.toasts()).toEqual([]);
  });

  it('empilha avisos na ordem em que foram criados', () => {
    service.error('primeiro');
    service.success('segundo');

    expect(service.toasts().map((toast) => toast.message)).toEqual(['primeiro', 'segundo']);
    expect(service.toasts().map((toast) => toast.kind)).toEqual(['error', 'success']);
  });

  it('dá ids diferentes para mensagens iguais', () => {
    service.error('falhou');
    service.error('falhou');

    const [first, second] = service.toasts();
    expect(first.id).not.toBe(second.id);
  });

  it('remove só o aviso pedido', () => {
    service.error('fica');
    service.error('sai');
    const target = service.toasts()[1];

    service.dismiss(target.id);

    expect(service.toasts().map((toast) => toast.message)).toEqual(['fica']);
  });

  it('some sozinho depois de 5 segundos', () => {
    service.error('temporário');
    expect(service.toasts()).toHaveLength(1);

    vi.advanceTimersByTime(5000);

    expect(service.toasts()).toHaveLength(0);
  });

  it('conta o tempo de cada aviso separadamente', () => {
    service.error('antigo');
    vi.advanceTimersByTime(3000);
    service.error('novo');

    vi.advanceTimersByTime(2000);
    expect(service.toasts().map((toast) => toast.message)).toEqual(['novo']);

    vi.advanceTimersByTime(3000);
    expect(service.toasts()).toHaveLength(0);
  });

  it('não quebra se o aviso já tiver sido fechado à mão antes do tempo', () => {
    service.error('fechado à mão');
    service.dismiss(service.toasts()[0].id);

    expect(() => vi.advanceTimersByTime(5000)).not.toThrow();
    expect(service.toasts()).toEqual([]);
  });
});
