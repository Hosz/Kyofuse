import { Component, computed, inject, input, output, signal } from '@angular/core';
import { AuthFieldComponent } from '../auth-field/auth-field';
import { PasswordStrengthMeterComponent } from '../password-strength-meter/password-strength-meter';
import { RobotCheckComponent } from '../robot-check/robot-check';
import { registerRequest } from '../../../models/auth/register-form.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService } from '../../../core/i18n/i18n.service';

const MIN_AGE = 18;

@Component({
  selector: 'app-register-form',
  imports: [AuthFieldComponent, PasswordStrengthMeterComponent, RobotCheckComponent, TranslatePipe],
  templateUrl: './register-form.html',
  styleUrl: './register-form.css',
})
export class RegisterFormComponent {
  readonly i18n = inject(I18nService);
  loading = input(false);
  submitRegister = output<registerRequest>();

  readonly today = new Date().toISOString().split('T')[0];

  firstName = signal('');
  lastName = signal('');
  username = signal('');
  email = signal('');
  password = signal('');
  confirmPassword = signal('');
  robotVerified = signal(false);

  showPassword = signal(false);
  showConfirmPassword = signal(false);

  firstNameTouched = signal(false);
  lastNameTouched = signal(false);
  usernameTouched = signal(false);
  emailTouched = signal(false);
  passwordTouched = signal(false);
  confirmPasswordTouched = signal(false);
  submitAttempted = signal(false);

  private readonly emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  firstNameError = computed(() => {
    if (!this.firstName()) return this.i18n.t('auth.firstNameRequired');
    return null;
  });

  lastNameError = computed(() => {
    if (!this.lastName()) return this.i18n.t('auth.lastNameRequired');
    if (this.lastName().length < 2) return this.i18n.t('auth.lastNameMin');
    return null;
  });

  usernameError = computed(() => {
    if (!this.username()) return this.i18n.t('auth.usernameRequired');
    if (this.username().length < 3) return this.i18n.t('auth.usernameMin');
    if (!/^[a-zA-Z0-9_]+$/.test(this.username())) return this.i18n.t('auth.usernamePattern');
    return null;
  });

  emailError = computed(() => {
    if (!this.email()) return this.i18n.t('auth.emailRequired');
    if (!this.emailPattern.test(this.email())) return this.i18n.t('auth.emailInvalid');
    return null;
  });

  passwordStrengthScore = computed(() => {
    const value = this.password();
    if (!value) return 0;
    let score = 0;
    if (value.length >= 8) score++;
    if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score++;
    if (/\d/.test(value)) score++;
    if (/[^A-Za-z0-9]/.test(value)) score++;
    return score;
  });

  passwordError = computed(() => {
    if (!this.password()) return this.i18n.t('auth.passwordCreateRequired');
    if (this.password().length < 8) return this.i18n.t('auth.passwordMin');
    return null;
  });

  confirmPasswordError = computed(() => {
    if (!this.confirmPassword()) return this.i18n.t('auth.confirmPasswordRequired');
    if (this.confirmPassword() !== this.password()) return this.i18n.t('auth.passwordsDoNotMatch');
    return null;
  });

  robotError = computed(() => (this.robotVerified() ? null : this.i18n.t('auth.robotRequired')));

  showFirstNameError = computed(() => (this.firstNameTouched() || this.submitAttempted()) && !!this.firstNameError());
  showLastNameError = computed(() => (this.lastNameTouched() || this.submitAttempted()) && !!this.lastNameError());
  showUsernameError = computed(() => (this.usernameTouched() || this.submitAttempted()) && !!this.usernameError());
  showEmailError = computed(() => (this.emailTouched() || this.submitAttempted()) && !!this.emailError());
  showPasswordError = computed(
    () => (this.passwordTouched() || this.submitAttempted()) && !!this.passwordError(),
  );
  showConfirmPasswordError = computed(
    () => (this.confirmPasswordTouched() || this.submitAttempted()) && !!this.confirmPasswordError(),
  );
  showRobotError = computed(() => this.submitAttempted() && !!this.robotError());

  isValid = computed(
    () =>
      !this.firstNameError() &&
      !this.lastNameError() &&
      !this.usernameError() &&
      !this.emailError() &&
      !this.passwordError() &&
      !this.confirmPasswordError() &&
      !this.robotError(),
  );

  onFirstNameChange(value: string): void {
    this.firstName.set(value);
  }

  onLastNameChange(value: string): void {
    this.lastName.set(value);
  }

  onUsernameChange(value: string): void {
    this.username.set(value);
  }

  onEmailChange(value: string): void {
    this.email.set(value);
  }

  onPasswordChange(value: string): void {
    this.password.set(value);
  }

  onConfirmPasswordChange(value: string): void {
    this.confirmPassword.set(value);
  }

  onRobotVerifiedChange(value: boolean): void {
    this.robotVerified.set(value);
  }

  toggleShowPassword(): void {
    this.showPassword.update((value) => !value);
  }

  toggleShowConfirmPassword(): void {
    this.showConfirmPassword.update((value) => !value);
  }

  onSubmit(): void {
    this.submitAttempted.set(true);
    if (!this.isValid()) return;

    this.submitRegister.emit({
      firstName: this.firstName(),
      lastName: this.lastName(),
      email: this.email(),
      username: this.username(),
      password: this.password(),
    });
  }
}
