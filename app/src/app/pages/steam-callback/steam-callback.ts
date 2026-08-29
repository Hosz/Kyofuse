import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthHeroComponent } from '../../components/auth/auth-hero/auth-hero';
import { AuthService } from '../../core/services/auth/auth.service';
import { isMfaRequired } from '../../models/auth/auth-response.model';

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

  authenticating = signal(true);
  success = signal(false);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const queryParams = this.route.snapshot.queryParams;

    if (!queryParams || Object.keys(queryParams).length === 0) {
      this.authenticating.set(false);
      this.error.set('Nenhum parâmetro de autenticação OpenID foi recebido da Steam.');
      return;
    }

    const mode = queryParams['openid.mode'] || queryParams['mode'];
    if (mode === 'cancel' || mode === 'error') {
      this.authenticating.set(false);
      this.error.set('A autenticação com a Steam foi cancelada ou recusada.');
      return;
    }

    const openIdParams: Record<string, string> = {};
    for (const key of Object.keys(queryParams)) {
      openIdParams[key] = String(queryParams[key]);
    }

    this.authService.loginWithSteam(openIdParams).subscribe({
      next: (result) => {
        this.authenticating.set(false);

        if (isMfaRequired(result)) {
          // Se tiver 2FA ativo, redireciona para a página de login preservando o mfaToken
          this.router.navigate([''], { queryParams: { mfaToken: result.mfaToken } });
          return;
        }

        this.success.set(true);
        setTimeout(() => {
          this.router.navigateByUrl('/home');
        }, 1500);
      },
      error: (err) => {
        this.authenticating.set(false);
        this.error.set(
          err?.error?.message ?? 'Falha ao autenticar com a conta Steam. Tente novamente.',
        );
      },
    });
  }
}
