import { Component, inject, input, output, signal } from '@angular/core';
import { ModalComponent } from '../modal/modal';
import { BlockService } from '../../../core/services/block/block.service';

@Component({
  selector: 'app-user-options-menu',
  imports: [ModalComponent],
  templateUrl: './user-options-menu.html',
  styleUrl: './user-options-menu.css',
})
export class UserOptionsMenuComponent {
  userId = input.required<string>();

  /** Emitido depois que o bloqueio é confirmado com sucesso no backend. */
  blocked = output<void>();

  private blockService = inject(BlockService);

  menuOpen = signal(false);
  confirmOpen = signal(false);
  blocking = signal(false);
  error = signal<string | null>(null);

  toggleMenu(): void {
    this.menuOpen.set(!this.menuOpen());
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  openBlockConfirm(): void {
    this.closeMenu();
    this.error.set(null);
    this.confirmOpen.set(true);
  }

  closeBlockConfirm(): void {
    if (this.blocking()) return;
    this.confirmOpen.set(false);
  }

  confirmBlock(): void {
    if (this.blocking()) return;
    this.blocking.set(true);
    this.error.set(null);

    this.blockService.blockUser(this.userId()).subscribe({
      next: () => {
        this.blocking.set(false);
        this.confirmOpen.set(false);
        this.blocked.emit();
      },
      error: (error) => {
        console.error('Failed to block user:', error);
        this.blocking.set(false);
        this.error.set('Não foi possível bloquear esse usuário. Tente novamente.');
      },
    });
  }
}
