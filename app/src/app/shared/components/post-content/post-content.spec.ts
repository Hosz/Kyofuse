import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PostContentComponent } from './post-content';
import { I18nService } from '../../../core/i18n/i18n.service';

describe('PostContentComponent', () => {
  let component: PostContentComponent;
  let fixture: ComponentFixture<PostContentComponent>;
  let i18nService: I18nService;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [PostContentComponent],
      providers: [provideRouter([]), I18nService],
    }).compileComponents();

    i18nService = TestBed.inject(I18nService);
    i18nService.setLanguage('pt');

    fixture = TestBed.createComponent(PostContentComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('deve renderizar conteúdo curto sem botão de ver mais', () => {
    fixture.componentRef.setInput('content', 'Texto curto simples');
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Texto curto simples');
    expect(compiled.querySelector('button')).toBeNull();
    expect(component.analysis().isLong).toBe(false);
  });

  it('deve identificar conteúdo longo e exibir botão de ver mais', () => {
    const longText = 'A'.repeat(350);
    fixture.componentRef.setInput('content', longText);
    fixture.detectChanges();

    expect(component.analysis().isLong).toBe(true);
    expect(component.expanded()).toBe(false);

    const compiled = fixture.nativeElement as HTMLElement;
    const button = compiled.querySelector('button');
    expect(button).not.toBeNull();
    expect(button?.textContent?.trim()).toBe(i18nService.t('feed.showMore'));
  });

  it('deve expandir o conteúdo ao clicar em ver mais e permitir recolher', () => {
    const longText = 'Primeira parte. ' + 'B'.repeat(300);
    fixture.componentRef.setInput('content', longText);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    let button = compiled.querySelector('button');
    expect(button?.textContent?.trim()).toBe(i18nService.t('feed.showMore'));

    // Clica em "Ver mais"
    button?.click();
    fixture.detectChanges();

    expect(component.expanded()).toBe(true);
    button = compiled.querySelector('button');
    expect(button?.textContent?.trim()).toBe(i18nService.t('feed.showLess'));
    expect(compiled.textContent).toContain(longText);

    // Clica em "Ver menos"
    button?.click();
    fixture.detectChanges();

    expect(component.expanded()).toBe(false);
    button = compiled.querySelector('button');
    expect(button?.textContent?.trim()).toBe(i18nService.t('feed.showMore'));
  });

  it('não deve truncar se collapsible for false', () => {
    const longText = 'C'.repeat(350);
    fixture.componentRef.setInput('content', longText);
    fixture.componentRef.setInput('collapsible', false);
    fixture.detectChanges();

    expect(component.analysis().isLong).toBe(false);
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('button')).toBeNull();
    expect(compiled.textContent).toContain(longText);
  });

  it('deve identificar como longo se tiver mais linhas que o limite', () => {
    const multilineText = 'Linha 1\nLinha 2\nLinha 3\nLinha 4\nLinha 5\nLinha 6\nLinha 7';
    fixture.componentRef.setInput('content', multilineText);
    fixture.detectChanges();

    expect(component.analysis().isLong).toBe(true);
  });

  it('deve renderizar menções como links com routerLink', () => {
    fixture.componentRef.setInput('content', 'Olá @hokyozu e $time e //comunidade');
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const links = compiled.querySelectorAll('a');
    expect(links.length).toBe(3);
    expect(links[0].getAttribute('href')).toBe('/perfil/hokyozu');
    expect(links[1].getAttribute('href')).toBe('/times/time');
    expect(links[2].getAttribute('href')).toBe('/comunidade/comunidade');
  });
});
