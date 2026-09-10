import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CharLimitIndicatorComponent } from './char-limit-indicator';

describe('CharLimitIndicatorComponent', () => {
  let component: CharLimitIndicatorComponent;
  let fixture: ComponentFixture<CharLimitIndicatorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CharLimitIndicatorComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CharLimitIndicatorComponent);
    component = fixture.componentInstance;
  });

  it('não deve renderizar nada quando current for 0', () => {
    fixture.componentRef.setInput('current', 0);
    fixture.componentRef.setInput('max', 500);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('svg')).toBeNull();
  });

  it('deve renderizar anel de progresso sem texto quando longe do limite', () => {
    fixture.componentRef.setInput('current', 250);
    fixture.componentRef.setInput('max', 500);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('svg')).not.toBeNull();
    expect(compiled.querySelector('span')).toBeNull();
    expect(component.isWarning()).toBe(false);
    expect(component.isExceeded()).toBe(false);
  });

  it('deve exibir contador numérico quando estiver próximo do limite (<= 20 restantes)', () => {
    fixture.componentRef.setInput('current', 485);
    fixture.componentRef.setInput('max', 500);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const span = compiled.querySelector('span');
    expect(span).not.toBeNull();
    expect(span?.textContent?.trim()).toBe('15');
    expect(component.isWarning()).toBe(true);
    expect(component.isExceeded()).toBe(false);
  });

  it('deve exibir 0 quando atingir o limite exato', () => {
    fixture.componentRef.setInput('current', 500);
    fixture.componentRef.setInput('max', 500);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const span = compiled.querySelector('span');
    expect(span?.textContent?.trim()).toBe('0');
    expect(component.isExceeded()).toBe(false);
  });

  it('deve exibir valor negativo em vermelho (ex: -10) quando ultrapassar o limite', () => {
    fixture.componentRef.setInput('current', 510);
    fixture.componentRef.setInput('max', 500);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const span = compiled.querySelector('span');
    expect(span).not.toBeNull();
    expect(span?.textContent?.trim()).toBe('-10');
    expect(span?.classList.contains('text-error')).toBe(true);
    expect(component.isExceeded()).toBe(true);
  });
});
