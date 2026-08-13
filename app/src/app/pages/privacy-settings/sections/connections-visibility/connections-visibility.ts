import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PrivacySettingsStore } from '../../privacy-settings-store';
import { PROFILE_VISIBILITY_OPTIONS } from '../../../../shared/models/privacy-options.model';

@Component({
  selector: 'app-connections-visibility-section',
  imports: [RouterLink],
  templateUrl: './connections-visibility.html',
  styleUrl: './connections-visibility.css',
})
export class ConnectionsVisibilitySectionComponent {
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
