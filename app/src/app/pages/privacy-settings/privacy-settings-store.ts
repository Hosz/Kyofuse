import { Injectable, inject, signal } from '@angular/core';
import { PrivacyService } from '../../core/services/privacy/privacy.service';
import { UserPrivacySettingsRequest, UserPrivacySettingsResponse } from '../../models/privacy/privacy.model';
import {
  FriendRequestPermission,
  MessagePermission,
  ProfileVisibility,
  TeamInvitePermission,
} from '../../shared/models/privacy-options.model';

const DEFAULT_VISIBILITY: ProfileVisibility = 'PUBLIC';

@Injectable()
export class PrivacySettingsStore {
  private privacyService = inject(PrivacyService);

  loading = signal(true);
  saving = signal(false);
  error = signal<string | null>(null);

  profileVisibility = signal<ProfileVisibility>(DEFAULT_VISIBILITY);
  messagePermission = signal<MessagePermission>('EVERYONE');
  friendRequestPermission = signal<FriendRequestPermission>('EVERYONE');
  teamInvitePermission = signal<TeamInvitePermission>('EVERYONE');

  private loaded = false;

  load(): void {
    if (this.loaded) return;
    this.loaded = true;

    this.privacyService.getSettings().subscribe({
      next: (settings) => {
        this.seedFromSettings(settings);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch privacy settings:', error);
        this.error.set('Não foi possível carregar suas configurações de privacidade.');
        this.loading.set(false);
      },
    });
  }

  save(onSuccess: () => void, onError: () => void): void {
    if (this.saving()) return;

    this.saving.set(true);
    this.error.set(null);

    const request: UserPrivacySettingsRequest = {
      profileVisibility: this.profileVisibility(),
      messagePermission: this.messagePermission(),
      friendRequestPermission: this.friendRequestPermission(),
      teamInvitePermission: this.teamInvitePermission(),
    };

    this.privacyService.setSettings(request).subscribe({
      next: () => {
        this.saving.set(false);
        onSuccess();
      },
      error: (error) => {
        console.error('Failed to save privacy settings:', error);
        this.saving.set(false);
        this.error.set('Não foi possível salvar suas configurações. Tente novamente.');
        onError();
      },
    });
  }

  private seedFromSettings(settings: UserPrivacySettingsResponse): void {
    this.profileVisibility.set(settings.profileVisibility);
    this.messagePermission.set(settings.messagePermission);
    this.friendRequestPermission.set(settings.friendRequestPermission);
    this.teamInvitePermission.set(settings.teamInvitePermission);
  }
}
