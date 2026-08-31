import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthHeroComponent } from '../../components/auth/auth-hero/auth-hero';
import { AuthTabsComponent } from '../../components/auth/auth-tabs/auth-tabs';
import { ForgotPasswordFormComponent } from '../../components/auth/forgot-password-form/forgot-password-form';
import { LoginFormComponent } from '../../components/auth/login-form/login-form';
import { MfaVerifyFormComponent } from '../../components/auth/mfa-verify-form/mfa-verify-form';
import { RegisterFormComponent } from '../../components/auth/register-form/register-form';
import { SocialAuthButtonsComponent } from '../../components/auth/social-auth-buttons/social-auth-buttons';
import { AuthTabId } from '../../shared/models/auth.model';
import { registerRequest } from '../../models/auth/register-form.model';
import { loginRequest } from '../../models/auth/login-form.model';
import { isMfaRequired } from '../../models/auth/auth-response.model';
import { AuthService } from '../../core/services/auth/auth.service';

@Component({
  selector: 'app-auth',
  imports: [
    AuthHeroComponent,
    AuthTabsComponent,
    ForgotPasswordFormComponent,
    LoginFormComponent,
    MfaVerifyFormComponent,
    RegisterFormComponent,
    SocialAuthButtonsComponent,
  ],
  templateUrl: './auth.html',
  styleUrl: './auth.css',
})
export class AuthComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  activeTab = signal<AuthTabId>('login');

  isLoggingIn = signal(false);
  isRegistering = signal(false);
  isSocialLoading = signal(false);

  loginError = signal<string | null>(null);
  registerError = signal<string | null>(null);

  /** Sucesso no cadastro: exibe aviso para checar o e-mail */
  registrationSuccess = signal(false);
  registeredEmail = signal('');
  resendingEmail = signal(false);
  resendSuccess = signal(false);
  resendError = signal<string | null>(null);

  /** Não-nulo quando o login exigiu 2FA: guarda o token curto até o código ser confirmado. */
  mfaToken = signal<string | null>(null);
  mfaSubmitting = signal(false);
  mfaError = signal<string | null>(null);

  /** Controle de exibição do formulário de Esqueci a Senha */
  isForgotPassword = signal(false);
  forgotPasswordSubmitting = signal(false);
  forgotPasswordError = signal<string | null>(null);
  forgotPasswordSuccess = signal(false);

  onTabSelected(tab: AuthTabId): void {
    this.activeTab.set(tab);
    this.loginError.set(null);
    this.registerError.set(null);
    this.registrationSuccess.set(false);
  }

  toggleTab(): void {
    this.activeTab.set(this.activeTab() === 'login' ? 'register' : 'login');
    this.loginError.set(null);
    this.registerError.set(null);
    this.registrationSuccess.set(false);
  }

  onLogin(payload: loginRequest): void {
    if (this.isLoggingIn()) return;
    this.isLoggingIn.set(true);
    this.loginError.set(null);

    this.authService.login(payload).subscribe({
      next: (result) => {
        this.isLoggingIn.set(false);
        if (isMfaRequired(result)) {
          this.mfaError.set(null);
          this.mfaToken.set(result.mfaToken);
          return;
        }

        this.router.navigateByUrl('/home');
      },
      error: (error) => {
        this.isLoggingIn.set(false);
        this.loginError.set(
          error?.error?.message ?? 'Falha ao autenticar. Verifique suas credenciais.',
        );
      },
    });
  }

  onGoogleCredentialReceived(idToken: string): void {
    if (this.isSocialLoading()) return;
    this.isSocialLoading.set(true);
    this.loginError.set(null);

    this.authService.loginWithGoogle(idToken).subscribe({
      next: (result) => {
        this.isSocialLoading.set(false);
        if (isMfaRequired(result)) {
          this.mfaError.set(null);
          this.mfaToken.set(result.mfaToken);
          return;
        }

        this.router.navigateByUrl('/home');
      },
      error: (error) => {
        this.isSocialLoading.set(false);
        this.loginError.set(
          error?.error?.message ?? 'Falha na autenticação com a conta Google.',
        );
      },
    });
  }

  onSteamClick(): void {
    if (this.isSocialLoading()) return;
    this.isSocialLoading.set(true);
    this.loginError.set(null);

    let channel: BroadcastChannel | null = null;
    try {
      if (typeof BroadcastChannel !== 'undefined') {
        channel = new BroadcastChannel('kyofuse_steam_auth');
      }
    } catch {}

    const cleanup = () => {
      if (channel) {
        channel.close();
      }
      window.removeEventListener('message', handleMessage);
      window.removeEventListener('storage', handleStorage);
      clearInterval(timer);
      this.isSocialLoading.set(false);
    };

    const processEvent = (data: any) => {
      if (!data?.type) return;

      if (data.type === 'STEAM_LOGIN_SUCCESS') {
        cleanup();
        if (data.mfaToken) {
          this.mfaError.set(null);
          this.mfaToken.set(data.mfaToken);
          return;
        }
        this.router.navigateByUrl('/home');
      } else if (data.type === 'STEAM_LOGIN_ERROR') {
        cleanup();
        this.loginError.set(data.message || 'Falha ao autenticar com a conta Steam.');
      } else if (data.type === 'STEAM_CANCEL') {
        cleanup();
      }
    };

    if (channel) {
      channel.onmessage = (event) => processEvent(event.data);
    }

    const handleMessage = (event: MessageEvent) => {
      if (event.origin !== window.location.origin) return;
      processEvent(event.data);
    };
    window.addEventListener('message', handleMessage);

    const handleStorage = (event: StorageEvent) => {
      if (event.key === 'kyofuse_steam_event' && event.newValue) {
        try {
          const data = JSON.parse(event.newValue);
          processEvent(data);
        } catch {}
      }
    };
    window.addEventListener('storage', handleStorage);

    const popup = this.authService.openSteamPopup('login');
    if (!popup) {
      cleanup();
      this.authService.redirectToSteam('login');
      return;
    }

    const timer = setInterval(() => {
      if (popup.closed) {
        setTimeout(() => cleanup(), 500);
      }
    }, 1000);
  }

  onRegister(payload: registerRequest): void {
    if (this.isRegistering()) return;
    this.isRegistering.set(true);
    this.registerError.set(null);

    this.authService.register(payload).subscribe({
      next: () => {
        this.isRegistering.set(false);
        this.registeredEmail.set(payload.email);
        this.registrationSuccess.set(true);
      },
      error: (error) => {
        this.isRegistering.set(false);
        this.registerError.set(
          error?.error?.message ?? 'Falha ao criar a conta. Tente novamente.',
        );
      },
    });
  }

  onResendRegistrationEmail(): void {
    const email = this.registeredEmail();
    if (!email || this.resendingEmail()) return;

    this.resendingEmail.set(true);
    this.resendError.set(null);
    this.resendSuccess.set(false);

    this.authService.resendVerificationEmail(email).subscribe({
      next: () => {
        this.resendingEmail.set(false);
        this.resendSuccess.set(true);
      },
      error: (err) => {
        this.resendingEmail.set(false);
        this.resendError.set(
          err?.error?.message ?? 'Falha ao reenviar link de ativação.',
        );
      },
    });
  }

  onBackToLogin(): void {
    this.registrationSuccess.set(false);
    this.activeTab.set('login');
  }

  onVerifyMfa(code: string): void {
    const mfaToken = this.mfaToken();
    if (!mfaToken || this.mfaSubmitting()) return;

    this.mfaSubmitting.set(true);
    this.mfaError.set(null);

    this.authService.verifyMfa({ mfaToken, code }).subscribe({
      next: () => {
        this.router.navigateByUrl('/home');
      },
      error: (error) => {
        this.mfaSubmitting.set(false);
        this.mfaError.set(error?.error?.message ?? 'Código inválido. Confira o app autenticador e tente novamente.');
      },
    });
  }

  onCancelMfa(): void {
    this.mfaToken.set(null);
    this.mfaError.set(null);
    this.mfaSubmitting.set(false);
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigateByUrl('');
      },
      error: () => {
        this.authService.clearSession();
        this.router.navigateByUrl('');
      },
    });
  }

  onOpenForgotPassword(): void {
    this.isForgotPassword.set(true);
    this.forgotPasswordError.set(null);
    this.forgotPasswordSuccess.set(false);
  }

  onCloseForgotPassword(): void {
    this.isForgotPassword.set(false);
    this.forgotPasswordError.set(null);
    this.forgotPasswordSuccess.set(false);
    this.forgotPasswordSubmitting.set(false);
  }

  onRequestForgotPassword(emailOrUsername: string): void {
    if (this.forgotPasswordSubmitting()) return;

    this.forgotPasswordSubmitting.set(true);
    this.forgotPasswordError.set(null);

    this.authService.forgotPassword({ emailOrUsername }).subscribe({
      next: () => {
        this.forgotPasswordSubmitting.set(false);
        this.forgotPasswordSuccess.set(true);
      },
      error: (error) => {
        this.forgotPasswordSubmitting.set(false);
        this.forgotPasswordError.set(
          error?.error?.message ?? 'Falha ao solicitar recuperação de senha. Tente novamente mais tarde.',
        );
      },
    });
  }
}
