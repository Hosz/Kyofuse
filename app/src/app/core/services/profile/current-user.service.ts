import { computed, inject, Injectable, signal } from '@angular/core';
import { ProfileService } from './profile.service';
import { AuthService } from '../auth/auth.service';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { I18nService } from '../../i18n/i18n.service';

/**
 * Id e username do usuário logado, buscado uma vez e compartilhado. Vários componentes precisam
 * saber "sou eu?" (apagar post, ações de dono, ocultar botão de bloquear) e antes disso cada um chamava
 * /profile/me por conta própria.
 */
@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  private profileService = inject(ProfileService);
  private authService = inject(AuthService);
  private i18nService = inject(I18nService);

  private id = signal<string | null>(null);
  private username = signal<string | null>(null);
  private requested = false;

  readonly userId = computed(() => {
    this.ensureLoaded();
    return this.id();
  });

  readonly userUsername = computed(() => {
    this.ensureLoaded();
    return this.username();
  });

  isMe(userIdOrUsername: string | null | undefined): boolean {
    if (!userIdOrUsername) return false;
    this.ensureLoaded();
    const myId = this.id();
    const myUsername = this.username();
    return (
      (!!myId && myId === userIdOrUsername) ||
      (!!myUsername && myUsername.toLowerCase() === userIdOrUsername.toLowerCase())
    );
  }

  setProfile(profile: gamerProfileResponse | { userId?: string; username?: string; country?: string }): void {
    if (profile.userId) this.id.set(profile.userId);
    if (profile.username) this.username.set(profile.username);
    if ('country' in profile && profile.country) {
      this.i18nService.initFromCountry(profile.country);
    }
  }

  private ensureLoaded(): void {
    if (this.requested && (this.id() || this.username())) return;
    this.requested = true;

    this.profileService.myProfile().subscribe({
      next: (profile) => {
        this.id.set(profile.userId);
        this.username.set(profile.username);
        if (profile.country) {
          this.i18nService.initFromCountry(profile.country);
        }
      },
      error: () => {
        // Fallback pra authService.me se myProfile falhar
        this.authService.me().subscribe({
          next: (me) => {
            this.id.set(me.userId);
            this.username.set(me.username);
          },
          error: () => {
            this.requested = false;
          },
        });
      },
    });
  }
}
