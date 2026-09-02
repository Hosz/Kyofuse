import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthHeroComponent } from '../../components/auth/auth-hero/auth-hero';
import { AuthFieldComponent } from '../../components/auth/auth-field/auth-field';
import { PasswordStrengthMeterComponent } from '../../components/auth/password-strength-meter/password-strength-meter';
import { AuthService } from '../../core/services/auth/auth.service';

@Component({
  selector: 'app-reset-password',
  imports: [
    AuthHeroComponent,
    AuthFieldComponent,
    PasswordStrengthMeterComponent,
    RouterLink,
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPasswordComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  token = signal('');
  validatingToken = signal(true);
  tokenValid = signal(false);
  tokenError = signal<string | null>(null);

  newPassword = signal('');
  confirmPassword = signal('');
  showPassword = signal(false);
  showConfirmPassword = signal(false);

  passwordTouched = signal(false);
  confirmPasswordTouched = signal(false);
  submitAttempted = signal(false);
  submitting = signal(false);
  submitSuccess = signal(false);
  submitError = signal<string | null>(null);

  passwordStrengthScore = computed(() => {
    const value = this.newPassword();
    if (!value) return 0;
    let score = 0;
    if (value.length >= 8) score++;
    if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score++;
    if (/\d/.test(value)) score++;
    if (/[^A-Za-z0-9]/.test(value)) score++;
    return score;
  });

  passwordError = computed(() => {
    if (!this.newPassword()) return 'Crie uma nova senha de acesso.';
    if (this.newPassword().length < 8) return 'A senha deve ter no mínimo 8 caracteres.';
    return null;
  });

  confirmPasswordError = computed(() => {
    if (!this.confirmPassword()) return 'Confirme a nova senha.';
    if (this.confirmPassword() !== this.newPassword()) return 'As senhas não coincidem.';
    return null;
  });

  showPasswordError = computed(
    () => (this.passwordTouched() || this.submitAttempted()) && !!this.passwordError(),
  );

  showConfirmPasswordError = computed(
    () => (this.confirmPasswordTouched() || this.submitAttempted()) && !!this.confirmPasswordError(),
  );

  isValid = computed(() => !this.passwordError() && !this.confirmPasswordError());

  ngOnInit(): void {
    const tokenParam = this.route.snapshot.queryParamMap.get('token');
    if (!tokenParam || !tokenParam.trim()) {
      this.validatingToken.set(false);
      this.tokenValid.set(false);
      this.tokenError.set('Nenhum código de recuperação foi encontrado no link.');
      return;
    }

    const cleanToken = tokenParam.trim();
    this.token.set(cleanToken);

    this.authService.validateResetToken(cleanToken).subscribe({
      next: () => {
        this.validatingToken.set(false);
        this.tokenValid.set(true);
      },
      error: (err) => {
        this.validatingToken.set(false);
        this.tokenValid.set(false);
        this.tokenError.set(
          err?.error?.message ?? 'Este link de recuperação é inválido ou já expirou. Solicite um novo link.',
        );
      },
    });
  }

  onPasswordChange(value: string): void {
    this.newPassword.set(value);
  }

  onConfirmPasswordChange(value: string): void {
    this.confirmPassword.set(value);
  }

  toggleShowPassword(): void {
    this.showPassword.update((val) => !val);
  }

  toggleShowConfirmPassword(): void {
    this.showConfirmPassword.update((val) => !val);
  }

  onSubmit(): void {
    this.submitAttempted.set(true);
    if (!this.isValid() || this.submitting()) return;

    this.submitting.set(true);
    this.submitError.set(null);

    this.authService
      .resetPassword({
        token: this.token(),
        newPassword: this.newPassword(),
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.submitSuccess.set(true);
          setTimeout(() => {
            this.router.navigateByUrl('');
          }, 3000);
        },
        error: (err) => {
          this.submitting.set(false);
          this.submitError.set(
            err?.error?.message ?? 'Falha ao redefinir a senha. Tente solicitar um novo link.',
          );
        },
      });
  }
}
