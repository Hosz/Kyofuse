import { Component, input, output } from '@angular/core';
import { ModalComponent } from '../modal/modal';

/**
 * Confirmação para ações que não dão pra desfazer. Existia uma cópia dessa lógica
 * dentro do menu de opções do usuário; agora todas as ações destrutivas passam por aqui.
 */
@Component({
  selector: 'app-confirm-dialog',
  imports: [ModalComponent],
  templateUrl: './confirm-dialog.html',
  styleUrl: './confirm-dialog.css',
})
export class ConfirmDialogComponent {
  open = input(false);
  title = input.required<string>();
  message = input.required<string>();
  confirmLabel = input('Confirmar');
  cancelLabel = input('Cancelar');
  /** Pinta o botão principal de vermelho — para remover, apagar, bloquear. */
  destructive = input(true);
  pending = input(false);
  error = input<string | null>(null);

  confirmed = output<void>();
  cancelled = output<void>();
}
