import { Injectable, inject, signal } from '@angular/core';
import { PrivacyService } from '../../core/services/privacy/privacy.service';
import { UserPrivacySettingsRequest, UserPrivacySettingsResponse } from '../../models/privacy/privacy.model';
import {
  DuoInvitePermission,
  FollowPermission,
  FriendRequestPermission,
  MessagePermission,
  ProfileVisibility,
  TeamInvitePermission,
} from '../../shared/models/privacy-options.model';

const DEFAULT_VISIBILITY: ProfileVisibility = 'PUBLIC';

/**
 * Guarda as 12 configurações de privacidade em memória enquanto o usuário navega
 * entre as sub-rotas de /configuracoes. Necessário porque o backend só aceita um
 * PUT com o objeto inteiro — não dá pra salvar um campo isolado — então cada seção
 * precisa saber o valor atual das outras pra não sobrescrevê-las com o default ao salvar.
 * Registrado como provider da rota pai (não providedIn: 'root'), então uma instância
 * nova é criada ao entrar em /configuracoes e destruída ao sair.
 */
@Injectable()
export class PrivacySettingsStore {
  private privacyService = inject(PrivacyService);

  loading = signal(true);
  saving = signal(false);
  error = signal<string | null>(null);

  profileVisibility = signal<ProfileVisibility>(DEFAULT_VISIBILITY);
  postVisibility = signal<ProfileVisibility>(DEFAULT_VISIBILITY);
  likesVisibility = signal<ProfileVisibility>(DEFAULT_VISIBILITY);
  repostsVisibility = signal<ProfileVisibility>(DEFAULT_VISIBILITY);
  friendsVisibility = signal<ProfileVisibility>(DEFAULT_VISIBILITY);
  followersVisibility = signal<ProfileVisibility>(DEFAULT_VISIBILITY);
  followingVisibility = signal<ProfileVisibility>(DEFAULT_VISIBILITY);
  messagePermission = signal<MessagePermission>('EVERYONE');
  friendRequestPermission = signal<FriendRequestPermission>('EVERYONE');
  followPermission = signal<FollowPermission>('EVERYONE');
  teamInvitePermission = signal<TeamInvitePermission>('EVERYONE');
  duoInvitePermission = signal<DuoInvitePermission>('FRIENDS');

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

  /** Envia sempre o objeto completo (com os valores atuais de todas as seções),
   * já que o backend não aceita atualização parcial. */
  save(onSuccess: () => void, onError: () => void): void {
    if (this.saving()) return;

    this.saving.set(true);
    this.error.set(null);

    const request: UserPrivacySettingsRequest = {
      profileVisibility: this.profileVisibility(),
      postVisibility: this.postVisibility(),
      likesVisibility: this.likesVisibility(),
      repostsVisibility: this.repostsVisibility(),
      friendsVisibility: this.friendsVisibility(),
      followersVisibility: this.followersVisibility(),
      followingVisibility: this.followingVisibility(),
      messagePermission: this.messagePermission(),
      friendRequestPermission: this.friendRequestPermission(),
      followPermission: this.followPermission(),
      teamInvitePermission: this.teamInvitePermission(),
      duoInvitePermission: this.duoInvitePermission(),
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
    this.postVisibility.set(settings.postVisibility);
    this.likesVisibility.set(settings.likesVisibility);
    this.repostsVisibility.set(settings.repostsVisibility);
    this.friendsVisibility.set(settings.friendsVisibility);
    this.followersVisibility.set(settings.followersVisibility);
    this.followingVisibility.set(settings.followingVisibility);
    this.messagePermission.set(settings.messagePermission);
    this.friendRequestPermission.set(settings.friendRequestPermission);
    this.followPermission.set(settings.followPermission);
    this.teamInvitePermission.set(settings.teamInvitePermission);
    this.duoInvitePermission.set(settings.duoInvitePermission);
  }
}
