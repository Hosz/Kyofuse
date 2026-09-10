import { Component, inject, signal } from '@angular/core';
import { UserSessionService } from '../../../core/services/auth/user-session.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-trust-device-modal',
  imports: [TranslatePipe],
  templateUrl: './trust-device-modal.html',
  styleUrl: './trust-device-modal.css',
})
export class TrustDeviceModalComponent {
  readonly userSessionService = inject(UserSessionService);
  private readonly toastService = inject(ToastService);
  private readonly i18n = inject(I18nService);

  loading = signal(false);

  confirm(): void {
    if (this.loading()) return;
    this.loading.set(true);

    this.userSessionService.trustCurrentDevice().subscribe({
      next: () => {
        this.toastService.success(this.i18n.t('auth.deviceTrustedSuccess'));
        this.userSessionService.showTrustPrompt.set(false);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to trust device:', err);
        this.userSessionService.showTrustPrompt.set(false);
        this.loading.set(false);
      },
    });
  }

  dismiss(): void {
    this.userSessionService.showTrustPrompt.set(false);
  }
}
