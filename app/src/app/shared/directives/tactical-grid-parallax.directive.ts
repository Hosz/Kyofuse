import { Directive, ElementRef, HostListener, inject } from '@angular/core';

/**
 * Aplica um leve deslocamento (paralaxe) ao elemento decorado,
 * acompanhando a posição do mouse na janela.
 * Substitui o `addEventListener('mousemove', ...)` do script original.
 *
 * Uso: <div class="tactical-grid" appTacticalGridParallax></div>
 */
@Directive({
  selector: '[appTacticalGridParallax]',
  standalone: true,
})
export class TacticalGridParallaxDirective {
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  private static readonly INTENSITY_PX = 10;

  @HostListener('window:mousemove', ['$event'])
  onWindowMouseMove(event: MouseEvent): void {
    const ratioX = event.clientX / window.innerWidth;
    const ratioY = event.clientY / window.innerHeight;

    const offsetX = ratioX * TacticalGridParallaxDirective.INTENSITY_PX;
    const offsetY = ratioY * TacticalGridParallaxDirective.INTENSITY_PX;

    this.elementRef.nativeElement.style.transform = `translate(${offsetX}px, ${offsetY}px)`;
  }
}
