import { Component, HostListener, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CookieConsentService } from '../../../core/services/ui/cookie-consent.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-cookie-preferences-modal',
  imports: [RouterLink, TranslatePipe],
  templateUrl: './cookie-preferences-modal.html',
  styleUrl: './cookie-preferences-modal.css',
})
export class CookiePreferencesModalComponent {
  readonly cookieService = inject(CookieConsentService);
  private readonly toastService = inject(ToastService);
  private readonly i18n = inject(I18nService);

  readonly functionalEnabled = signal<boolean>(true);
  readonly analyticsEnabled = signal<boolean>(false);

  constructor() {
    effect(() => {
      const isVisible = this.cookieService.modalVisible();
      if (isVisible) {
        const prefs = this.cookieService.preferences();
        untracked(() => {
          this.functionalEnabled.set(prefs.functional);
          this.analyticsEnabled.set(prefs.analytics);
        });
      }
    });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.cookieService.modalVisible()) {
      this.close();
    }
  }

  toggleFunctional(): void {
    this.functionalEnabled.update((val) => !val);
  }

  toggleAnalytics(): void {
    this.analyticsEnabled.update((val) => !val);
  }

  save(): void {
    this.cookieService.saveCustomPreferences(
      this.functionalEnabled(),
      this.analyticsEnabled()
    );
    this.toastService.success(this.i18n.t('cookieConsent.preferencesSaved'));
  }

  acceptAll(): void {
    this.cookieService.acceptAll();
    this.toastService.success(this.i18n.t('cookieConsent.preferencesSaved'));
  }

  close(): void {
    this.cookieService.closePreferences();
  }
}
