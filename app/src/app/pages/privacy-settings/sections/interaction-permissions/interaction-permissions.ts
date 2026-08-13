import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PrivacySettingsStore } from '../../privacy-settings-store';
import {
  DUO_INVITE_PERMISSION_OPTIONS,
  FOLLOW_PERMISSION_OPTIONS,
  FRIEND_REQUEST_PERMISSION_OPTIONS,
  MESSAGE_PERMISSION_OPTIONS,
  TEAM_INVITE_PERMISSION_OPTIONS,
} from '../../../../shared/models/privacy-options.model';

@Component({
  selector: 'app-interaction-permissions-section',
  imports: [RouterLink],
  templateUrl: './interaction-permissions.html',
  styleUrl: './interaction-permissions.css',
})
export class InteractionPermissionsSectionComponent {
  readonly store = inject(PrivacySettingsStore);

  readonly messagePermissionOptions = MESSAGE_PERMISSION_OPTIONS;
  readonly friendRequestPermissionOptions = FRIEND_REQUEST_PERMISSION_OPTIONS;
  readonly followPermissionOptions = FOLLOW_PERMISSION_OPTIONS;
  readonly teamInvitePermissionOptions = TEAM_INVITE_PERMISSION_OPTIONS;
  readonly duoInvitePermissionOptions = DUO_INVITE_PERMISSION_OPTIONS;

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
