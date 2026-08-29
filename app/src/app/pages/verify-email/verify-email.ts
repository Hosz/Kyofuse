import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthHeroComponent } from '../../components/auth/auth-hero/auth-hero';
import { AuthService } from '../../core/services/auth/auth.service';

@Component({
  selector: 'app-verify-email',
  imports: [AuthHeroComponent, RouterLink],
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.css',
})
export class VerifyEmailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  token = signal('');
  validatingToken = signal(true);
  tokenValid = signal(false);
  tokenError = signal<string | null>(null);

  verifying = signal(false);
  verifySuccess = signal(false);
  verifyError = signal<string | null>(null);

  resendEmail = signal('');
  resending = signal(false);
  resendSuccess = signal(false);
  resendError = signal<string | null>(null);

  ngOnInit(): void {
    const tokenParam = this.route.snapshot.queryParamMap.get('token');
    if (!tokenParam || !tokenParam.trim()) {
      this.validatingToken.set(false);
      this.tokenValid.set(false);
      this.tokenError.set('Nenhum código de confirmação foi encontrado no link.');
      return;
    }

    const cleanToken = tokenParam.trim();
    this.token.set(cleanToken);

    // Valida o token e já realiza a ativação
    this.authService.validateEmailVerificationToken(cleanToken).subscribe({
      next: () => {
        this.validatingToken.set(false);
        this.tokenValid.set(true);
        this.onConfirmVerification();
      },
      error: (err) => {
        this.validatingToken.set(false);
        this.tokenValid.set(false);
        this.tokenError.set(
          err?.error?.message ?? 'Este link de confirmação é inválido ou já expirou.',
        );
      },
    });
  }

  onConfirmVerification(): void {
    if (this.verifying()) return;

    this.verifying.set(true);
    this.verifyError.set(null);

    this.authService.verifyEmail(this.token()).subscribe({
      next: () => {
        this.verifying.set(false);
        this.verifySuccess.set(true);
        setTimeout(() => {
          this.router.navigateByUrl('/home');
        }, 2500);
      },
      error: (err) => {
        this.verifying.set(false);
        this.verifyError.set(
          err?.error?.message ?? 'Falha ao confirmar o e-mail. Solicite um novo link.',
        );
      },
    });
  }

  onResendVerification(): void {
    const email = this.resendEmail().trim();
    if (!email || this.resending()) return;

    this.resending.set(true);
    this.resendError.set(null);
    this.resendSuccess.set(false);

    this.authService.resendVerificationEmail(email).subscribe({
      next: () => {
        this.resending.set(false);
        this.resendSuccess.set(true);
      },
      error: (err) => {
        this.resending.set(false);
        this.resendError.set(
          err?.error?.message ?? 'Falha ao reenviar e-mail de confirmação.',
        );
      },
    });
  }
}
