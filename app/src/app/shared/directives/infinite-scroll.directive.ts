import { Directive, ElementRef, effect, inject, input, output } from '@angular/core';

/**
 * Dispara `loadMore` quando o elemento entra na tela. Colocado como sentinela no fim de
 * uma lista, substitui o botão "Carregar mais" sem tirá-lo do caminho de quem prefere
 * clicar — o botão continua funcionando se for mantido no template.
 */
@Directive({
  selector: '[appInfiniteScroll]',
})
export class InfiniteScrollDirective {
  /** Falso enquanto não há mais páginas ou uma requisição já está em voo. */
  canLoad = input(false, { alias: 'appInfiniteScroll' });

  loadMore = output<void>();

  private element = inject(ElementRef<HTMLElement>);

  constructor() {
    effect((onCleanup) => {
      if (!this.canLoad()) return;

      const observer = new IntersectionObserver(
        (entries) => {
          if (entries.some((entry) => entry.isIntersecting)) this.loadMore.emit();
        },
        // Começa a carregar antes de a sentinela aparecer, pra a lista não "engasgar".
        { rootMargin: '400px' },
      );

      observer.observe(this.element.nativeElement);
      onCleanup(() => observer.disconnect());
    });
  }
}
