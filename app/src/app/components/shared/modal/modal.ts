import {
  Component,
  ElementRef,
  HostListener,
  effect,
  input,
  output,
  viewChild,
} from '@angular/core';

const FOCUSABLE =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

let modalIdCounter = 0;

@Component({
  selector: 'app-modal',
  imports: [],
  templateUrl: './modal.html',
  styleUrl: './modal.css',
})
export class ModalComponent {
  open = input(false);
  title = input.required<string>();

  /**
   * 'compact' encolhe o painel para o tamanho de um menu de opções — usado quando o
   * modal carrega só alguns botões e ocupar a largura inteira do diálogo padrão
   * pareceria desproporcional.
   */
  size = input<'default' | 'compact'>('default');

  closed = output<void>();

  readonly titleId = `modal-title-${modalIdCounter++}`;

  private readonly panel = viewChild<ElementRef<HTMLElement>>('panel');

  constructor() {
    effect(() => {
      if (this.open()) {
        queueMicrotask(() => this.panel()?.nativeElement.focus());
      }
    });
  }

  /**
   * Prende o Tab dentro do diálogo. Sem isso, tabular a partir do último botão leva o
   * foco para a página atrás do modal — quem navega por teclado ou leitor de tela fica
   * mexendo num conteúdo que visualmente está coberto.
   */
  @HostListener('document:keydown.tab', ['$event'])
  @HostListener('document:keydown.shift.tab', ['$event'])
  onTab(rawEvent: Event): void {
    const event = rawEvent as KeyboardEvent;
    const panel = this.panel()?.nativeElement;
    if (!this.open() || !panel) return;

    const focusable = Array.from(panel.querySelectorAll<HTMLElement>(FOCUSABLE))
      .filter((element) => element.offsetParent !== null);
    if (focusable.length === 0) {
      event.preventDefault();
      panel.focus();
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    const active = document.activeElement as HTMLElement | null;

    if (event.shiftKey && (active === first || active === panel)) {
      event.preventDefault();
      last.focus();
      return;
    }

    if (!event.shiftKey && active === last) {
      event.preventDefault();
      first.focus();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.open()) {
      this.closed.emit();
    }
  }

  onBackdropClick(): void {
    this.closed.emit();
  }
}
