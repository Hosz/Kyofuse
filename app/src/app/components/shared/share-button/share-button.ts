import { Component, OnDestroy, input, signal } from '@angular/core';

@Component({
  selector: 'app-share-button',
  imports: [],
  templateUrl: './share-button.html',
  styleUrl: './share-button.css',
})
export class ShareButtonComponent implements OnDestroy {
  /** Caminho relativo (ex.: "/post/123") ou URL completa a ser copiada. */
  path = input.required<string>();

  toastVisible = signal(false);
  toastPosition = signal({ x: 0, y: 0 });

  private hideTimeout?: ReturnType<typeof setTimeout>;

  ngOnDestroy(): void {
    if (this.hideTimeout) clearTimeout(this.hideTimeout);
  }

  share(event: MouseEvent): void {
    event.stopPropagation();

    const value = this.path();
    const url = value.startsWith('http') ? value : `${window.location.origin}${value}`;

    navigator.clipboard
      .writeText(url)
      .then(() => this.showToast(event.clientX, event.clientY))
      .catch((error) => console.error('Failed to copy link:', error));
  }

  private showToast(x: number, y: number): void {
    this.toastPosition.set({ x, y });
    this.toastVisible.set(true);

    if (this.hideTimeout) clearTimeout(this.hideTimeout);
    this.hideTimeout = setTimeout(() => this.toastVisible.set(false), 1800);
  }
}
