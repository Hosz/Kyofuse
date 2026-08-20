import { Directive, ElementRef, NgZone, OnDestroy, computed, inject, signal } from '@angular/core';

/** Distância em px a partir da qual o gesto vira arraste e deixa de valer como clique. */
const DRAG_THRESHOLD = 4;

/** Quanto uma seta rola por clique, como fração da largura visível. */
const ARROW_SCROLL_RATIO = 0.8;

/**
 * Faz um contêiner rolar na horizontal arrastando com o mouse, do jeito que a barra de
 * abas do X funciona, e expõe se ainda há conteúdo para cada lado — o componente usa
 * isso para mostrar as setas só quando elas têm serventia.
 *
 * Exportada como `dragScroll` para o template alcançar o estado:
 * `<div appDragScroll #strip="dragScroll">` e então `strip.canScrollRight()`.
 */
@Directive({
  selector: '[appDragScroll]',
  exportAs: 'dragScroll',
  host: {
    '(pointerdown)': 'onPointerDown($event)',
    '(pointermove)': 'onPointerMove($event)',
    '(pointerup)': 'onPointerUp($event)',
    '(pointercancel)': 'onPointerUp($event)',
    '(click)': 'onClick($event)',
    '(scroll)': 'updateEdges()',
  },
})
export class DragScrollDirective implements OnDestroy {
  private readonly host = inject(ElementRef<HTMLElement>);
  private readonly zone = inject(NgZone);

  private readonly scrollLeftValue = signal(0);
  private readonly scrollWidthValue = signal(0);
  private readonly clientWidthValue = signal(0);

  private pointerId: number | null = null;
  private startX = 0;
  private startScrollLeft = 0;
  private dragged = false;
  private observer?: ResizeObserver;

  canScrollLeft = computed(() => this.scrollLeftValue() > 1);

  canScrollRight = computed(
    () => this.scrollLeftValue() + this.clientWidthValue() < this.scrollWidthValue() - 1,
  );

  constructor() {
    // O conteúdo muda quando o usuário fixa/desafixa uma comunidade, e a largura muda
    // ao redimensionar a janela: os dois alteram se ainda cabe rolagem.
    this.zone.runOutsideAngular(() => {
      this.observer = new ResizeObserver(() => this.zone.run(() => this.updateEdges()));
      this.observer.observe(this.element);
      for (const child of Array.from(this.element.children)) {
        this.observer.observe(child);
      }
    });

    queueMicrotask(() => this.updateEdges());
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private get element(): HTMLElement {
    return this.host.nativeElement;
  }

  updateEdges(): void {
    this.scrollLeftValue.set(this.element.scrollLeft);
    this.scrollWidthValue.set(this.element.scrollWidth);
    this.clientWidthValue.set(this.element.clientWidth);
  }

  scrollByStep(direction: -1 | 1): void {
    this.element.scrollBy({
      left: direction * this.element.clientWidth * ARROW_SCROLL_RATIO,
      behavior: 'smooth',
    });
  }

  onPointerDown(event: PointerEvent): void {
    // Só arrasto com o botão principal do mouse; toque já rola nativamente.
    if (event.pointerType === 'touch' || event.button !== 0) return;

    this.pointerId = event.pointerId;
    this.startX = event.clientX;
    this.startScrollLeft = this.element.scrollLeft;
    this.dragged = false;
  }

  onPointerMove(event: PointerEvent): void {
    if (this.pointerId !== event.pointerId) return;

    const delta = event.clientX - this.startX;
    if (!this.dragged && Math.abs(delta) < DRAG_THRESHOLD) return;

    if (!this.dragged) {
      this.dragged = true;
      this.element.setPointerCapture(event.pointerId);
    }

    this.element.scrollLeft = this.startScrollLeft - delta;
    this.updateEdges();
  }

  onPointerUp(event: PointerEvent): void {
    if (this.pointerId !== event.pointerId) return;

    if (this.element.hasPointerCapture(event.pointerId)) {
      this.element.releasePointerCapture(event.pointerId);
    }
    this.pointerId = null;
  }

  /** Impede que o arraste termine selecionando a aba que estava sob o cursor. */
  onClick(event: MouseEvent): void {
    if (!this.dragged) return;
    event.preventDefault();
    event.stopPropagation();
    this.dragged = false;
  }
}
