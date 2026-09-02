import { Component, computed, input, output, signal } from '@angular/core';
import { AuthFieldComponent } from '../auth-field/auth-field';

@Component({
  selector: 'app-mfa-verify-form',
  imports: [AuthFieldComponent],
  templateUrl: './mfa-verify-form.html',
  styleUrl: './mfa-verify-form.css',
})
export class MfaVerifyFormComponent {
  submitCode = output<string>();
  back = output<void>();

  /** Erro vindo do backend (código inválido, token expirado, rate limit...). */
  errorMessage = input<string | null>(null);
  submitting = input(false);

  code = signal('');
  codeTouched = signal(false);
  submitAttempted = signal(false);

  codeError = computed(() => {
    if (!this.code().trim()) return 'Informe o código do autenticador ou um código de backup.';
    return null;
  });

  showCodeError = computed(() => (this.codeTouched() || this.submitAttempted()) && !!this.codeError());

  onCodeChange(value: string): void {
    this.code.set(value);
  }

  onSubmit(): void {
    this.submitAttempted.set(true);
    if (this.codeError()) return;

    this.submitCode.emit(this.code().trim());
  }

  onBack(): void {
    this.back.emit();
  }
}
