import { Component, inject, input, output, signal } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { CommunityService } from '../../../core/services/communities/community.service';
import { MediaService } from '../../../core/services/media/media.service';
import { CommunityResponse, CommunityVisibility } from '../../../models/communities/community.model';
import { toSlug, COMMUNITY_FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';

@Component({
  selector: 'app-create-community-modal',
  imports: [ModalComponent, TranslatePipe],
  templateUrl: './create-community-modal.html',
  styleUrl: './create-community-modal.css',
})
export class CreateCommunityModalComponent {
  open = input(false);

  closed = output<void>();
  created = output<CommunityResponse>();

  private communityService = inject(CommunityService);
  private mediaService = inject(MediaService);

  name = signal('');
  slug = signal('');
  description = signal('');
  avatarUrl = signal('');
  bannerUrl = signal('');
  visibility = signal<CommunityVisibility>('PUBLIC');

  uploadingAvatar = signal(false);
  uploadingBanner = signal(false);
  readonly defaultAvatar = COMMUNITY_FALLBACK_AVATAR_URL;
  saving = signal(false);
  error = signal<string | null>(null);

  /** Mesma regra do formulário de criar time: o slug segue o nome até o usuário
   * editá-lo manualmente. */
  private slugTouched = false;

  onNameChange(value: string): void {
    this.name.set(value);
    if (!this.slugTouched) this.slug.set(toSlug(value));
  }

  onSlugChange(value: string): void {
    this.slugTouched = true;
    this.slug.set(value);
  }

  onAvatarFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadingAvatar.set(true);
    this.mediaService.uploadImage(file).subscribe({
      next: (res) => {
        this.avatarUrl.set(res.url);
        this.uploadingAvatar.set(false);
      },
      error: (err) => {
        console.error('Failed to upload avatar:', err);
        this.uploadingAvatar.set(false);
      },
    });
  }

  onBannerFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadingBanner.set(true);
    this.mediaService.uploadImage(file).subscribe({
      next: (res) => {
        this.bannerUrl.set(res.url);
        this.uploadingBanner.set(false);
      },
      error: (err) => {
        console.error('Failed to upload banner:', err);
        this.uploadingBanner.set(false);
      },
    });
  }

  removeAvatar(): void {
    this.avatarUrl.set('');
  }

  removeBanner(): void {
    this.bannerUrl.set('');
  }

  onClose(): void {
    if (this.saving()) return;
    this.reset();
    this.closed.emit();
  }

  submit(): void {
    if (this.saving()) return;
    if (!this.name().trim() || !this.slug().trim()) {
      this.error.set('Nome e slug são obrigatórios.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    this.communityService
      .createCommunity({
        communityName: this.name().trim(),
        communitySlug: this.slug().trim(),
        communityDescription: this.description().trim() || undefined,
        communityAvatarUrl: this.avatarUrl().trim() || undefined,
        communityBannerUrl: this.bannerUrl().trim() || undefined,
        visibility: this.visibility(),
      })
      .subscribe({
        next: (response) => {
          this.saving.set(false);
          this.created.emit(response);
          this.reset();
        },
        error: (error) => {
          console.error('Failed to create community:', error);
          this.saving.set(false);
          this.error.set('Não foi possível criar a comunidade. Verifique os dados.');
        },
      });
  }

  private reset(): void {
    this.name.set('');
    this.slug.set('');
    this.description.set('');
    this.avatarUrl.set('');
    this.bannerUrl.set('');
    this.visibility.set('PUBLIC');
    this.error.set(null);
    this.slugTouched = false;
  }
}
