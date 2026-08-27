import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthHeroComponent } from '../../components/auth/auth-hero/auth-hero';
import { AuthTabsComponent } from '../../components/auth/auth-tabs/auth-tabs';
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

  /** Não-nulo quando o login exigiu 2FA: guarda o token curto até o código ser confirmado. */
  mfaToken = signal<string | null>(null);
  mfaSubmitting = signal(false);
  mfaError = signal<string | null>(null);

  onTabSelected(tab: AuthTabId): void {
    this.activeTab.set(tab);
  }

  toggleTab(): void {
    this.activeTab.set(this.activeTab() === 'login' ? 'register' : 'login');
  }

  onLogin(payload: loginRequest): void {
    this.authService.login(payload).subscribe({
      next: (result) => {
        if (isMfaRequired(result)) {
          this.mfaError.set(null);
          this.mfaToken.set(result.mfaToken);
          return;
        }

        console.log('Login successful:', result);
        this.router.navigateByUrl('/home');
      },
      error: (error) => {
        console.error('Login failed:', error);
        // Handle login error (e.g., show an error message to the user)
      }
    });
  }

  onVerifyMfa(code: string): void {
    const mfaToken = this.mfaToken();
    if (!mfaToken || this.mfaSubmitting()) return;

    this.mfaSubmitting.set(true);
    this.mfaError.set(null);

    this.authService.verifyMfa({ mfaToken, code }).subscribe({
      next: (response) => {
        console.log('MFA verification successful:', response);
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

  onRegister(payload: registerRequest): void {
    this.authService.register(payload).subscribe({
      next: (response) => {
        console.log('Registration successful:', response);
        this.router.navigateByUrl('/home');
      },
      error: (error) => {
        console.error('Registration failed:', error);
        // Handle registration error (e.g., show an error message to the user)
      }
    });
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: (response) => {
        console.log('Logout successful:', response);
        this.router.navigateByUrl('');
      },
      error: (error) => {
        console.error('Logout failed:', error);
        this.authService.clearSession();
        this.router.navigateByUrl('');
      }
    });
  }


}
