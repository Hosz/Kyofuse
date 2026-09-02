import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthHeroComponent } from '../../components/auth/auth-hero/auth-hero';
import { AuthService } from '../../core/services/auth/auth.service';
import { UserAccountService } from '../../core/services/account/user-account.service';
import { isMfaRequired } from '../../models/auth/auth-response.model';

export interface SteamAuthMessage {
  type: 'STEAM_LINK_SUCCESS' | 'STEAM_LINK_ERROR' | 'STEAM_LOGIN_SUCCESS' | 'STEAM_LOGIN_ERROR' | 'STEAM_CANCEL';
  message?: string;
  isConflict?: boolean;
  mfaToken?: string;
  timestamp?: number;
}

@Component({
  selector: 'app-steam-callback',
  imports: [AuthHeroComponent, RouterLink],
  templateUrl: './steam-callback.html',
  styleUrl: './steam-callback.css',
})
export class SteamCallbackComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly accountService = inject(UserAccountService);

  authenticating = signal(true);
  isLinkAction = signal(false);
  isPopup = signal(false);
  success = signal(false);
  error = signal<string | null>(null);
  isConflictError = signal(false);

  ngOnInit(): void {
    const queryParams = this.route.snapshot.queryParams;
    const popupFlag = queryParams['popup'] === 'true' || window.name === 'SteamAuthPopup' || !!window.opener;
    this.isPopup.set(popupFlag);

    if (!queryParams || Object.keys(queryParams).length === 0) {
      this.authenticating.set(false);
      this.error.set('Nenhum parâmetro de autenticação OpenID foi recebido da Steam.');
      this.emitAndClose({
        type: 'STEAM_LINK_ERROR',
        message: 'Nenhum parâmetro recebido da Steam.',
      });
      return;
    }

    const mode = queryParams['openid.mode'] || queryParams['mode'];
    if (mode === 'cancel' || mode === 'error') {
      this.authenticating.set(false);
      this.error.set('A autenticação com a Steam foi cancelada ou recusada.');
      this.emitAndClose({
        type: 'STEAM_CANCEL',
        message: 'Autenticação cancelada.',
      });
      return;
    }

    const action = queryParams['action'];
    if (action === 'link') {
      this.isLinkAction.set(true);
    }

    const openIdParams: Record<string, string> = {};
    for (const key of Object.keys(queryParams)) {
      openIdParams[key] = String(queryParams[key]);
    }

    if (action === 'link') {
      this.accountService.linkSteam(openIdParams).subscribe({
        next: () => {
          this.authenticating.set(false);
          this.success.set(true);

          this.emitAndClose({
            type: 'STEAM_LINK_SUCCESS',
          });

          if (!popupFlag) {
            setTimeout(() => {
              this.router.navigateByUrl('/configuracoes/conta');
            }, 1500);
          }
        },
        error: (err) => {
          this.authenticating.set(false);
          const msg = err?.error?.message ?? 'Falha ao vincular a conta Steam ao seu perfil.';
          this.error.set(msg);
          const isConflict =
            err?.status === 409 ||
            msg.toLowerCase().includes('já está vinculada') ||
            msg.toLowerCase().includes('já está associada') ||
            msg.toLowerCase().includes('já pertence');

          if (isConflict) {
            this.isConflictError.set(true);
          }

          this.emitAndClose({
            type: 'STEAM_LINK_ERROR',
            message: msg,
            isConflict,
          });
        },
      });
      return;
    }

    this.authService.loginWithSteam(openIdParams).subscribe({
      next: (result) => {
        this.authenticating.set(false);
        this.success.set(true);

        const mfaToken = isMfaRequired(result) ? result.mfaToken : undefined;

        this.emitAndClose({
          type: 'STEAM_LOGIN_SUCCESS',
          mfaToken,
        });

        if (!popupFlag) {
          if (mfaToken) {
            this.router.navigate([''], { queryParams: { mfaToken } });
            return;
          }

          setTimeout(() => {
            this.router.navigateByUrl('/home');
          }, 1500);
        }
      },
      error: (err) => {
        this.authenticating.set(false);
        const msg = err?.error?.message ?? 'Falha ao autenticar com a conta Steam. Tente novamente.';
        this.error.set(msg);

        this.emitAndClose({
          type: 'STEAM_LOGIN_ERROR',
          message: msg,
        });
      },
    });
  }

  private emitAndClose(event: SteamAuthMessage): void {
    const payload: SteamAuthMessage = {
      ...event,
      timestamp: Date.now(),
    };

    // 1. BroadcastChannel (garante comunicação inter-janelas mesmo com COOP/opener severado)
    try {
      if (typeof BroadcastChannel !== 'undefined') {
        const channel = new BroadcastChannel('kyofuse_steam_auth');
        channel.postMessage(payload);
        setTimeout(() => channel.close(), 100);
      }
    } catch (e) {
      console.warn('Falha no BroadcastChannel:', e);
    }

    // 2. LocalStorage Event (segundo canal de redundância)
    try {
      localStorage.setItem('kyofuse_steam_event', JSON.stringify(payload));
    } catch (e) {
      console.warn('Falha no localStorage:', e);
    }

    // 3. PostMessage direto para window.opener (se disponível)
    try {
      if (window.opener && !window.opener.closed) {
        window.opener.postMessage(payload, window.location.origin);
      }
    } catch (e) {
      console.warn('Falha no postMessage:', e);
    }

    // 4. Se estiver em pop-up, fecha a janela imediatamente
    if (this.isPopup()) {
      setTimeout(() => {
        try {
          window.close();
        } catch {}
      }, 300);
    }
  }
}
