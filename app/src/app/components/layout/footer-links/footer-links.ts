import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../../core/services/ui/toast.service';

import { TranslatePipe } from '../../../core/i18n/translate.pipe';

import { CookieConsentService } from '../../../core/services/ui/cookie-consent.service';

@Component({
  selector: 'app-footer-links',
  imports: [RouterLink, TranslatePipe],
  templateUrl: './footer-links.html',
  styleUrl: './footer-links.css',
})
export class FooterLinksComponent {
  readonly cookieConsentService = inject(CookieConsentService);

  openCookiePreferences(): void {
    this.cookieConsentService.openPreferences();
  }
}
 
