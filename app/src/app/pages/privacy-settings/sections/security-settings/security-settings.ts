import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../../../core/services/auth/auth.service';
import { TwoFactorAuthService } from '../../../../core/services/auth/two-factor-auth.service';

type SecurityStep = 'status' | 'setup' | 'recovery-codes';

/** Ativação/desativação do 2FA (TOTP) da conta. Independente do PrivacySettingsStore
 * (não é uma preferência de privacidade) — fala direto com AuthService/TwoFactorAuthService. */
@Component({
  selector: 'app-security-settings-section',
  imports: [],
  templateUrl: './security-settings.html',
  styleUrl: './security-settings.css',
})
export class SecuritySettingsSectionComponent {
  private authService = inject(AuthService);
  private twoFactorAuthService = inject(TwoFactorAuthService);

  loading = signal(true);
  loadError = signal<string | null>(null);
  enabled = signal(false);
  step = signal<SecurityStep>('status');

  secret = signal<string | null>(null);
  otpauthUri = signal<string | null>(null);
  setupCode = signal('');
  setupError = signal<string | null>(null);
  submittingSetup = signal(false);
  secretCopied = signal(false);

  showDisableForm = signal(false);
  disablePassword = signal('');
  disableError = signal<string | null>(null);
  submittingDisable = signal(false);

  recoveryCodes = signal<string[]>([]);
  recoveryCodesCopied = signal(false);

  ngOnInit(): void {
    this.authService.me().subscribe({
      next: (me) => {
        this.enabled.set(me.totpEnabled);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch account status:', error);
        this.loadError.set('Não foi possível carregar o status da sua conta.');
        this.loading.set(false);
      },
    });
  }

  startSetup(): void {
    this.setupError.set(null);
    this.setupCode.set('');

    this.twoFactorAuthService.setup().subscribe({
      next: (response) => {
        this.secret.set(response.secret);
        this.otpauthUri.set(response.otpauthUri);
        this.step.set('setup');
      },
      error: (error) => {
        console.error('Failed to start 2FA setup:', error);
        this.setupError.set('Não foi possível iniciar a configuração do 2FA. Tente novamente.');
      },
    });
  }

  cancelSetup(): void {
    this.secret.set(null);
    this.otpauthUri.set(null);
    this.setupCode.set('');
    this.setupError.set(null);
    this.step.set('status');
  }

  confirmSetup(): void {
    const code = this.setupCode().trim();
    if (this.submittingSetup() || !code) return;

    this.submittingSetup.set(true);
    this.setupError.set(null);

    this.twoFactorAuthService.confirm(code).subscribe({
      next: (response) => {
        this.recoveryCodes.set(response.recoveryCodes);
        this.enabled.set(true);
        this.step.set('recovery-codes');
        this.submittingSetup.set(false);
      },
      error: (error) => {
        console.error('Failed to confirm 2FA setup:', error);
        this.submittingSetup.set(false);
        this.setupError.set(
          error?.error?.message ?? 'Código inválido. Confira o app autenticador e tente novamente.',
        );
      },
    });
  }

  finishRecoveryCodes(): void {
    this.recoveryCodes.set([]);
    this.recoveryCodesCopied.set(false);
    this.secret.set(null);
    this.otpauthUri.set(null);
    this.setupCode.set('');
    this.step.set('status');
  }

  copySecret(): void {
    const secret = this.secret();
    if (!secret) return;

    navigator.clipboard
      .writeText(secret)
      .then(() => {
        this.secretCopied.set(true);
        setTimeout(() => this.secretCopied.set(false), 2000);
      })
      .catch((error) => console.error('Failed to copy secret:', error));
  }

  copyRecoveryCodes(): void {
    navigator.clipboard
      .writeText(this.recoveryCodes().join('\n'))
      .then(() => {
        this.recoveryCodesCopied.set(true);
        setTimeout(() => this.recoveryCodesCopied.set(false), 2000);
      })
      .catch((error) => console.error('Failed to copy recovery codes:', error));
  }

  toggleDisableForm(): void {
    this.showDisableForm.update((value) => !value);
    this.disablePassword.set('');
    this.disableError.set(null);
  }

  confirmDisable(): void {
    const password = this.disablePassword();
    if (this.submittingDisable() || !password) return;

    this.submittingDisable.set(true);
    this.disableError.set(null);

    this.twoFactorAuthService.disable(password).subscribe({
      next: () => {
        this.enabled.set(false);
        this.showDisableForm.set(false);
        this.disablePassword.set('');
        this.submittingDisable.set(false);
      },
      error: (error) => {
        console.error('Failed to disable 2FA:', error);
        this.submittingDisable.set(false);
        this.disableError.set(error?.error?.message ?? 'Senha inválida.');
      },
    });
  }
}
