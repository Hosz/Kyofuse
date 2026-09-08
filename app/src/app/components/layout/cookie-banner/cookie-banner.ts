import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CookieConsentService } from '../../../core/services/ui/cookie-consent.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-cookie-banner',
  imports: [RouterLink, TranslatePipe],
  templateUrl: './cookie-banner.html',
  styleUrl: './cookie-banner.css',
})
export class CookieBannerComponent {
  readonly cookieService = inject(CookieConsentService);

  acceptAll(): void {
    this.cookieService.acceptAll();
  }

  acceptEssential(): void {
    this.cookieService.acceptEssentialOnly();
  }

  openPreferences(): void {
    this.cookieService.openPreferences();
  }
}
