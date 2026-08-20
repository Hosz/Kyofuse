import { Component, inject, input, output, signal } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { CommunityService } from '../../../core/services/communities/community.service';
import { CommunityResponse, CommunityVisibility } from '../../../models/communities/community.model';
import { toSlug } from '../../../shared/utils/format.util';

@Component({
  selector: 'app-create-community-modal',
  imports: [ModalComponent],
  templateUrl: './create-community-modal.html',
  styleUrl: './create-community-modal.css',
})
export class CreateCommunityModalComponent {
  open = input(false);

  closed = output<void>();
  created = output<CommunityResponse>();

  private communityService = inject(CommunityService);

  name = signal('');
  slug = signal('');
  description = signal('');
  avatarUrl = signal('');
  bannerUrl = signal('');
  visibility = signal<CommunityVisibility>('PUBLIC');

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
        next: (community) => {
          this.saving.set(false);
          this.reset();
          this.created.emit(community);
        },
        error: (error) => {
          console.error('Failed to create community:', error);
          this.saving.set(false);
          this.error.set(error?.error?.message ?? 'Não foi possível criar a comunidade. Confira os dados informados.');
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
