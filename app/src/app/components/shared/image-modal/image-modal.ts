import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-image-modal',
  imports: [],
  template: `
    @if (imageUrl()) {
      <div
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm transition-opacity"
        (click)="close.emit()"
      >
        <div class="relative max-h-[90vh] max-w-[90vw] overflow-hidden rounded-2xl bg-surface-container-low shadow-2xl" (click)="$event.stopPropagation()">
          <button
            type="button"
            (click)="close.emit()"
            class="absolute right-3 top-3 z-10 flex h-9 w-9 cursor-pointer items-center justify-center rounded-full bg-black/60 text-white backdrop-blur-md transition-colors hover:bg-black/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-container"
            aria-label="Fechar visualização"
          >
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
          <img
            [src]="imageUrl()"
            alt="Visualização da imagem"
            class="max-h-[85vh] max-w-[85vw] object-contain"
          />
        </div>
      </div>
    }
  `,
})
export class ImageModalComponent {
  imageUrl = input<string | null>(null);
  close = output<void>();
}
