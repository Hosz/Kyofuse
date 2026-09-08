import { AfterViewInit, Component, ElementRef, ViewChild, inject, input, output } from '@angular/core';
import { AuthService } from '../../../core/services/auth/auth.service';

declare const google: any;

@Component({
  selector: 'app-social-auth-buttons',
  imports: [],
  templateUrl: './social-auth-buttons.html',
  styleUrl: './social-auth-buttons.css',
})
export class SocialAuthButtonsComponent implements AfterViewInit {
  @ViewChild('googleBtnContainer') googleBtnContainer?: ElementRef<HTMLDivElement>;

  private authService = inject(AuthService);

  loading = input(false);
  disabled = input(false);

  googleCredential = output<string>();
  steamClick = output<void>();

  ngAfterViewInit(): void {
    this.initGoogleButton();
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
                this.googleCredential.emit(response.credential);
              }
            },
            ux_mode: 'popup',
            auto_select: false,
            cancel_on_tap_outside: true,
          });

          if (this.googleBtnContainer?.nativeElement) {
            google.accounts.id.renderButton(this.googleBtnContainer.nativeElement, {
              type: 'standard',
              shape: 'rectangular',
              theme: 'outline',
              text: 'signin_with',
              size: 'large',
              width: 250,
            });
          }
        } catch (e) {
          console.warn('Falha ao inicializar Google Sign-In:', e);
        }
      },
      error: (e) => console.warn('Falha ao carregar Google Client ID:', e),
    });
  }
}
