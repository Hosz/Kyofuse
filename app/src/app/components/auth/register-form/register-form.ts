import { Component, computed, input, output, signal } from '@angular/core';
import { AuthFieldComponent } from '../auth-field/auth-field';
import { PasswordStrengthMeterComponent } from '../password-strength-meter/password-strength-meter';
import { RobotCheckComponent } from '../robot-check/robot-check';
import { registerRequest } from '../../../models/auth/register-form.model';

const MIN_AGE = 18;

@Component({
  selector: 'app-register-form',
  imports: [AuthFieldComponent, PasswordStrengthMeterComponent, RobotCheckComponent],
  templateUrl: './register-form.html',
  styleUrl: './register-form.css',
})
export class RegisterFormComponent {
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
    if (!this.firstName()) return 'Informe seu nome.';
    return null;
  });

  lastNameError = computed(() => {
    if (!this.lastName()) return 'Informe seu sobrenome.';
    if (this.lastName().length < 2) return 'O sobrenome precisa ter ao menos 2 caracteres.';
    return null;
  })

  usernameError = computed(() => {
    if (!this.username()) return 'Escolha um username.';
    if (this.username().length < 3) return 'O username precisa ter ao menos 3 caracteres.';
    if (!/^[a-zA-Z0-9_]+$/.test(this.username())) return 'Use apenas letras, números e "_".';
    return null;
  });

  emailError = computed(() => {
    if (!this.email()) return 'Informe seu e-mail de contato.';
    if (!this.emailPattern.test(this.email())) return 'Informe um e-mail válido.';
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
    if (!this.password()) return 'Crie uma senha de acesso.';
    if (this.password().length < 8) return 'Mínimo de 8 caracteres.';
    return null;
  });

  confirmPasswordError = computed(() => {
    if (!this.confirmPassword()) return 'Repita a senha.';
    if (this.confirmPassword() !== this.password()) return 'As senhas não coincidem.';
    return null;
  });

  robotError = computed(() => (this.robotVerified() ? null : 'Confirme que você não é um robô.'));

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
