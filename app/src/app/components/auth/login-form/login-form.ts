import { Component, computed, inject, input, output, signal } from '@angular/core';
import { AuthFieldComponent } from '../auth-field/auth-field';
import { LoginPayload } from '../../../shared/models/auth.model';
import { loginRequest } from '../../../models/auth/login-form.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService } from '../../../core/i18n/i18n.service';

@Component({
  selector: 'app-login-form',
  imports: [AuthFieldComponent, TranslatePipe],
  templateUrl: './login-form.html',
  styleUrl: './login-form.css',
})
export class LoginFormComponent {
  readonly i18n = inject(I18nService);
  loading = input(false);
  submitLogin = output<loginRequest>();
  forgotPassword = output<void>();

  login = signal('');
  password = signal('');
  showPassword = signal(false);

  loginTouched = signal(false);
  passwordTouched = signal(false);
  submitAttempted = signal(false);

  loginError = computed(() => {
    if (!this.login()) return this.i18n.t('auth.loginRequired');
    return null;
  });

  passwordError = computed(() => {
    if (!this.password()) return this.i18n.t('auth.passwordRequired');
    return null;
  });

  showLoginError = computed(() => (this.loginTouched() || this.submitAttempted()) && !!this.loginError());
  showPasswordError = computed(
    () => (this.passwordTouched() || this.submitAttempted()) && !!this.passwordError(),
  );

  isValid = computed(() => !this.loginError() && !this.passwordError());

  onLoginChange(value: string): void {
    this.login.set(value);
  }

  onPasswordChange(value: string): void {
    this.password.set(value);
  }

  toggleShowPassword(): void {
    this.showPassword.update((value) => !value);
  }

  onSubmit(): void {
    this.submitAttempted.set(true);
    if (!this.isValid()) return;

    this.submitLogin.emit({ login: this.login(), password: this.password() });
  }
}
