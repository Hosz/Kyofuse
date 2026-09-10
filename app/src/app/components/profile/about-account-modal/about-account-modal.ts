import { Component, computed, inject, input, output } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { getCountryFlagUrl } from '../../../shared/models/location-options.model';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-about-account-modal',
  imports: [ModalComponent, TranslatePipe],
  templateUrl: './about-account-modal.html',
  styleUrl: './about-account-modal.css',
})
export class AboutAccountModalComponent {
  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  private i18n = inject(I18nService);

  open = input(false);
  profile = input.required<gamerProfileResponse>();
  closed = output<void>();

  countryDisplay = computed(() => {
    const prof = this.profile();
    return prof.registrationCountry || prof.country || this.i18n.t('profile.unknownLocation');
  });

  flagUrl = computed(() => {
    const prof = this.profile();
    const codeOrCountry = prof.registrationCountryCode || prof.registrationCountry || prof.country;
    return getCountryFlagUrl(codeOrCountry);
  });

  deviceDisplay = computed(() => {
    const prof = this.profile();
    return prof.registrationDevice || this.i18n.t('profile.unknownDevice');
  });

  formattedDate = computed(() => {
    const raw = this.profile().createdAt;
    if (!raw) return this.i18n.t('profile.unknownLocation');
    try {
      const date = new Date(raw);
      const lang = this.i18n.currentLang();
      return new Intl.DateTimeFormat(lang === 'pt' ? 'pt-BR' : lang, {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
      }).format(date);
    } catch {
      return raw;
    }
  });
}
