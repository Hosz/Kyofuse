import { Component, computed, inject, input, output, signal } from '@angular/core';
import { AuthFieldComponent } from '../auth-field/auth-field';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService } from '../../../core/i18n/i18n.service';

@Component({
  selector: 'app-forgot-password-form',
  imports: [AuthFieldComponent, TranslatePipe],
  templateUrl: './forgot-password-form.html',
  styleUrl: './forgot-password-form.css',
})
export class ForgotPasswordFormComponent {
  readonly i18n = inject(I18nService);
  submitting = input(false);
  errorMessage = input<string | null>(null);
  success = input(false);

  submitRequest = output<string>();
  back = output<void>();

  emailOrUsername = signal('');
  emailOrUsernameTouched = signal(false);
  submitAttempted = signal(false);

  emailOrUsernameError = computed(() => {
    const val = this.emailOrUsername().trim();
    if (!val) return this.i18n.t('auth.emailOrUsernameRequired');
    return null;
  });

  showEmailOrUsernameError = computed(
    () => (this.emailOrUsernameTouched() || this.submitAttempted()) && !!this.emailOrUsernameError(),
  );

  isValid = computed(() => !this.emailOrUsernameError());

  onEmailOrUsernameChange(value: string): void {
    this.emailOrUsername.set(value);
  }

  onSubmit(): void {
    this.submitAttempted.set(true);
    if (!this.isValid()) return;

    this.submitRequest.emit(this.emailOrUsername().trim());
  }

  onBack(): void {
    this.back.emit();
  }
}
