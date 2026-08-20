import { computed, inject, Injectable, signal } from '@angular/core';
import { ProfileService } from './profile.service';
import { AuthService } from '../auth/auth.service';

/**
 * Id do usuário logado, buscado uma vez e compartilhado. Vários componentes precisam
 * saber "sou eu?" (apagar post, ações de admin) e antes disso cada um chamava
 * /profile/me por conta própria.
 */
@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  private profileService = inject(ProfileService);
  private authService = inject(AuthService);

  private id = signal<string | null>(null);
  private requested = false;

  readonly userId = computed(() => {
    this.ensureLoaded();
    return this.id();
  });

  isMe(userId: string | null | undefined): boolean {
    const myId = this.userId();
    return !!myId && !!userId && myId === userId;
  }

  private ensureLoaded(): void {
    if (this.requested || !this.authService.isAuthenticated()) return;
    this.requested = true;

    this.profileService.myProfile().subscribe({
      next: (profile) => this.id.set(profile.userId),
      error: (error) => {
        console.error('Failed to fetch current user:', error);
        // Libera uma nova tentativa: sem o id, ações de dono ficam escondidas.
        this.requested = false;
      },
    });
  }
}
