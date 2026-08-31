import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserAccountService } from '../../core/services/account/user-account.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { MediaService } from '../../core/services/media/media.service';
import { FALLBACK_AVATAR_URL } from '../../shared/utils/format.util';
import { UserAccountResponse } from '../../models/account/user-account.model';
import { gamerProfileResponse } from '../../models/profile/gamer-profile.model';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-profile-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile-setup.html',
  styleUrls: ['./profile-setup.css'],
})
export class ProfileSetupComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly userAccountService = inject(UserAccountService);
  private readonly profileService = inject(ProfileService);
  private readonly mediaService = inject(MediaService);

  account = signal<UserAccountResponse | null>(null);
  profile = signal<gamerProfileResponse | null>(null);
  
  username = signal('');
  nickname = signal('');
  avatarUrl = signal('');
  
  loading = signal(true);
  saving = signal(false);
  uploadingAvatar = signal(false);
  errorMessage = signal<string | null>(null);
  avatarLoadError = signal(false);
  
  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  
  previewAvatar = computed(() => {
    if (this.avatarLoadError() || !this.avatarUrl()?.trim()) {
      return this.fallbackAvatar;
    }
    return this.avatarUrl().trim();
  });

  displayNickname = computed(() => this.nickname().trim() || 'Seu Apelido');
  displayUsername = computed(() => this.username().trim() || 'username');

  ngOnInit(): void {
    Promise.all([
      firstValueFrom(this.userAccountService.getAccount()).then((acc) => {
        this.account.set(acc);
        this.username.set(acc.username);
      }),
      firstValueFrom(this.profileService.myProfile()).then((prof) => {
        this.profile.set(prof);
        this.nickname.set(prof.nickname || '');
        this.avatarUrl.set(prof.avatarUrl || '');
      }),
    ])
      .catch((err) => {
        console.error('Falha ao inicializar dados de configuração:', err);
        this.errorMessage.set('Falha ao carregar seus dados. Tente recarregar a página.');
      })
      .finally(() => {
        this.loading.set(false);
      });
  }

  onAvatarFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadingAvatar.set(true);
    this.mediaService.uploadUserAvatar(file).subscribe({
      next: (res) => {
        this.avatarUrl.set(res.avatarUrl);
        this.uploadingAvatar.set(false);
        this.avatarLoadError.set(false);
      },
      error: (err) => {
        console.error('Falha ao enviar avatar:', err);
        this.uploadingAvatar.set(false);
      },
    });
  }

  onAvatarInput(value: string): void {
    this.avatarUrl.set(value);
    this.avatarLoadError.set(false);
  }

  onAvatarImageError(): void {
    if (this.avatarUrl().trim()) {
      this.avatarLoadError.set(true);
    }
  }

  clearAvatar(): void {
    this.avatarUrl.set('');
    this.avatarLoadError.set(false);
  }

  async save(): Promise<void> {
    const rawUsername = this.username().trim();
    const rawNickname = this.nickname().trim();

    if (!rawUsername) {
      this.errorMessage.set('Informe um nome de usuário.');
      return;
    }

    if (rawUsername.length < 3) {
      this.errorMessage.set('O nome de usuário deve ter no mínimo 3 caracteres.');
      return;
    }

    if (!/^[a-zA-Z0-9_]+$/.test(rawUsername)) {
      this.errorMessage.set('O nome de usuário pode conter apenas letras, números e sublinhado (_).');
      return;
    }

    if (!rawNickname) {
      this.errorMessage.set('Informe seu apelido (nickname).');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    
    try {
      const currAcc = this.account();
      if (currAcc && currAcc.username.toLowerCase() !== rawUsername.toLowerCase()) {
        await firstValueFrom(this.userAccountService.updateUsername({ username: rawUsername }));
      }
      
      await firstValueFrom(this.profileService.editProfile({
        nickname: rawNickname,
        avatarUrl: this.avatarLoadError() ? undefined : this.avatarUrl().trim() || undefined,
      }));

      await this.router.navigate(['/home']);
    } catch (error: any) {
      console.error('Erro ao salvar perfil:', error);
      this.saving.set(false);
      this.errorMessage.set(error?.error?.message ?? 'Falha ao salvar as alterações. Verifique os dados informados.');
    }
  }

  async skip(): Promise<void> {
    this.saving.set(true);
    this.errorMessage.set(null);
    try {
      const currentNickname = this.nickname().trim() || this.account()?.username || 'Player';
      await firstValueFrom(this.profileService.editProfile({
        nickname: currentNickname,
      }));
      await this.router.navigate(['/home']);
    } catch (error: any) {
      console.error('Erro ao pular configuração:', error);
      this.saving.set(false);
      this.errorMessage.set(error?.error?.message ?? 'Falha ao concluir a configuração.');
    }
  }
}
