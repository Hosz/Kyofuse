import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PrivacySettingsStore } from '../../privacy-settings-store';
import { PROFILE_VISIBILITY_OPTIONS } from '../../../../shared/models/privacy-options.model';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-profile-visibility-section',
  imports: [RouterLink, TranslatePipe],
  templateUrl: './profile-visibility.html',
  styleUrl: './profile-visibility.css',
})
export class ProfileVisibilitySectionComponent {
  readonly store = inject(PrivacySettingsStore);
  readonly options = PROFILE_VISIBILITY_OPTIONS;

  saved = signal(false);

  save(): void {
    this.saved.set(false);
    this.store.save(
      () => {
        this.saved.set(true);
        setTimeout(() => this.saved.set(false), 2500);
      },
      () => {},
    );
  }
}
