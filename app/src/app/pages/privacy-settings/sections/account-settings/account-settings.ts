import { Component, ElementRef, OnInit, ViewChild, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserAccountService } from '../../../../core/services/account/user-account.service';
import { AuthService } from '../../../../core/services/auth/auth.service';
import { UserAccountResponse } from '../../../../models/account/user-account.model';
import { SkeletonComponent } from '../../../../components/shared/skeleton/skeleton';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

declare const google: any;

@Component({
  selector: 'app-account-settings',
  imports: [FormsModule, SkeletonComponent, RouterLink, TranslatePipe],
  templateUrl: './account-settings.html',
  styleUrl: './account-settings.css',
})
export class AccountSettingsSectionComponent implements OnInit {
  @ViewChild('googleConnectBtn') googleConnectBtn?: ElementRef<HTMLDivElement>;

  private readonly accountService = inject(UserAccountService);
  private readonly authService = inject(AuthService);

  loading = signal(true);
  account = signal<UserAccountResponse | null>(null);
  loadError = signal<string | null>(null);

  // Edição de Username
  editingUsername = signal(false);
  newUsername = signal('');
  savingUsername = signal(false);
  usernameError = signal<string | null>(null);
  usernameSuccess = signal(false);

  // Edição de E-mail
  editingEmail = signal(false);
  newEmail = signal('');
  emailCurrentPassword = signal('');
  savingEmail = signal(false);
  emailError = signal<string | null>(null);
  emailSuccess = signal(false);

  // Vínculos Sociais (Google / Steam)
  actionLoading = signal(false);
  socialError = signal<string | null>(null);
  socialSuccess = signal<string | null>(null);

  ngOnInit(): void {
    this.loadAccount();
  }

  loadAccount(): void {
    this.loading.set(true);
    this.loadError.set(null);

    this.accountService.getAccount().subscribe({
      next: (acc) => {
        this.account.set(acc);
        this.newUsername.set(acc.username);
        this.newEmail.set(acc.isSyntheticEmail ? '' : acc.email);
        this.loading.set(false);

        if (!acc.hasGoogle) {
          setTimeout(() => this.initGoogleButton(), 100);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.loadError.set(err?.error?.message ?? 'Falha ao carregar informações da conta.');
      },
    });
  }

  private initGoogleButton(): void {
    if (typeof google === 'undefined' || !google.accounts?.id) {
      setTimeout(() => this.initGoogleButton(), 300);
      return;
    }

    this.authService.getGoogleClientId().subscribe({
      next: (clientId) => {
        if (!clientId) return;
        try {
          google.accounts.id.initialize({
            client_id: clientId,
            callback: (response: any) => {
              if (response?.credential) {
                this.linkGoogle(response.credential);
              }
            },
            ux_mode: 'popup',
            auto_select: false,
            cancel_on_tap_outside: true,
          });

          if (this.googleConnectBtn?.nativeElement) {
            google.accounts.id.renderButton(this.googleConnectBtn.nativeElement, {
              type: 'standard',
              shape: 'pill',
              theme: 'outline',
              text: 'signin_with',
              size: 'medium',
            });
          }
        } catch (e) {
          console.warn('Falha ao inicializar botão Google Sign-In:', e);
        }
      },
      error: (e) => console.warn('Falha ao carregar Google Client ID:', e),
    });
  }

  toggleEditUsername(): void {
    this.editingUsername.update((v) => !v);
    this.usernameError.set(null);
    this.usernameSuccess.set(false);
    if (this.account()) {
      this.newUsername.set(this.account()!.username);
    }
  }

  saveUsername(): void {
    const username = this.newUsername().trim();
    if (!username) {
      this.usernameError.set('Informe um nome de usuário.');
      return;
    }
    if (username.length < 3) {
      this.usernameError.set('O username deve ter ao menos 3 caracteres.');
      return;
    }
    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
      this.usernameError.set('Use apenas letras, números e sublinhado (_).');
      return;
    }
    if (!/[a-zA-Z]/.test(username)) {
      this.usernameError.set('O username deve conter no mínimo uma letra.');
      return;
    }

    this.savingUsername.set(true);
    this.usernameError.set(null);

    this.accountService.updateUsername({ username }).subscribe({
      next: (updated) => {
        this.account.set(updated);
        this.savingUsername.set(false);
        this.editingUsername.set(false);
        this.usernameSuccess.set(true);
        setTimeout(() => this.usernameSuccess.set(false), 3000);
      },
      error: (err) => {
        this.savingUsername.set(false);
        this.usernameError.set(err?.error?.message ?? 'Falha ao atualizar nome de usuário.');
      },
    });
  }

  toggleEditEmail(): void {
    this.editingEmail.update((v) => !v);
    this.emailError.set(null);
    this.emailSuccess.set(false);
    this.emailCurrentPassword.set('');
    if (this.account()) {
      this.newEmail.set(this.account()!.isSyntheticEmail ? '' : this.account()!.email);
    }
  }

  saveEmail(): void {
    const email = this.newEmail().trim().toLowerCase();
    if (!email) {
      this.emailError.set('Informe um endereço de e-mail.');
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      this.emailError.set('Informe um e-mail válido.');
      return;
    }

    if (this.account()?.hasPassword && !this.emailCurrentPassword()) {
      this.emailError.set('Informe sua senha atual para confirmar a alteração.');
      return;
    }

    this.savingEmail.set(true);
    this.emailError.set(null);

    this.accountService.updateEmail({
      email,
      currentPassword: this.emailCurrentPassword() || undefined,
    }).subscribe({
      next: (updated) => {
        this.account.set(updated);
        this.savingEmail.set(false);
        this.editingEmail.set(false);
        this.emailCurrentPassword.set('');
        this.emailSuccess.set(true);
        setTimeout(() => this.emailSuccess.set(false), 4000);
      },
      error: (err) => {
        this.savingEmail.set(false);
        this.emailError.set(err?.error?.message ?? 'Falha ao atualizar e-mail.');
      },
    });
  }

  linkGoogle(idToken: string): void {
    this.actionLoading.set(true);
    this.socialError.set(null);
    this.socialSuccess.set(null);

    this.accountService.linkGoogle(idToken).subscribe({
      next: (updated) => {
        this.account.set(updated);
        this.actionLoading.set(false);
        this.socialSuccess.set('Conta Google conectada com sucesso!');
        setTimeout(() => this.socialSuccess.set(null), 4000);
      },
      error: (err) => {
        this.actionLoading.set(false);
        this.socialError.set(err?.error?.message ?? 'Falha ao vincular conta Google.');
      },
    });
  }

  unlinkGoogle(): void {
    const acc = this.account();
    if (!acc) return;

    if (!acc.hasPassword || acc.isSyntheticEmail || !acc.emailVerified) {
      this.socialError.set(
        'Para desvincular a conta Google, você precisa ter uma senha cadastrada, um e-mail próprio e tê-lo verificado.'
      );
      return;
    }

    this.actionLoading.set(true);
    this.socialError.set(null);
    this.socialSuccess.set(null);

    this.accountService.unlinkGoogle().subscribe({
      next: (updated) => {
        this.account.set(updated);
        this.actionLoading.set(false);
        this.socialSuccess.set('Conta Google desvinculada com sucesso!');
        setTimeout(() => {
          this.socialSuccess.set(null);
          this.initGoogleButton();
        }, 300);
      },
      error: (err) => {
        this.actionLoading.set(false);
        this.socialError.set(err?.error?.message ?? 'Falha ao desvincular conta Google.');
      },
    });
  }

  connectSteam(): void {
    this.actionLoading.set(true);
    this.socialError.set(null);
    this.socialSuccess.set(null);

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
      this.actionLoading.set(false);
    };

    const processEvent = (data: any) => {
      if (!data?.type) return;

      if (data.type === 'STEAM_LINK_SUCCESS') {
        cleanup();
        this.socialSuccess.set('Conta Steam vinculada com sucesso ao seu perfil!');
        this.loadAccount();
        setTimeout(() => this.socialSuccess.set(null), 4000);
      } else if (data.type === 'STEAM_LINK_ERROR') {
        cleanup();
        this.socialError.set(data.message || 'Falha ao vincular conta Steam.');
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

    const popup = this.authService.openSteamPopup('link');
    if (!popup) {
      cleanup();
      this.socialError.set('O navegador bloqueou a janela pop-up da Steam. Permita pop-ups para conectar sua conta.');
      return;
    }

    const timer = setInterval(() => {
      if (popup.closed) {
        setTimeout(() => cleanup(), 500);
      }
    }, 1000);
  }

  unlinkSteam(): void {
    const acc = this.account();
    if (!acc) return;

    if (!acc.hasPassword || acc.isSyntheticEmail || !acc.emailVerified) {
      this.socialError.set(
        'Para desvincular a conta Steam, você precisa ter uma senha cadastrada, um e-mail próprio e tê-lo verificado.'
      );
      return;
    }

    this.actionLoading.set(true);
    this.socialError.set(null);
    this.socialSuccess.set(null);

    this.accountService.unlinkSteam().subscribe({
      next: (updated) => {
        this.account.set(updated);
        this.actionLoading.set(false);
        this.socialSuccess.set('Conta Steam desvinculada com sucesso!');
        setTimeout(() => this.socialSuccess.set(null), 4000);
      },
      error: (err) => {
        this.actionLoading.set(false);
        this.socialError.set(err?.error?.message ?? 'Falha ao desvincular conta Steam.');
      },
    });
  }
}
