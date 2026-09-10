import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService } from '../../../core/i18n/i18n.service';
import { CommunityService } from '../../../core/services/communities/community.service';
import { CommunityInviteService } from '../../../core/services/communities/community-invite.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { CommunityResponse } from '../../../models/communities/community.model';

const COMMUNITIES_PAGE_SIZE = 50;

type InviteState = 'idle' | 'sending' | 'sent' | 'failed';

@Component({
  selector: 'app-community-invite-modal',
  imports: [ModalComponent, TranslatePipe],
  templateUrl: './community-invite-modal.html',
  styleUrl: './community-invite-modal.css',
})
export class CommunityInviteModalComponent {
  open = input(false);
  targetName = input<string>('');
  targetUsername = input.required<string>();

  closed = output<void>();

  private communityService = inject(CommunityService);
  private communityInviteService = inject(CommunityInviteService);
  private toastService = inject(ToastService);
  readonly i18n = inject(I18nService);

  modalTitle = computed(() =>
    this.i18n.t('modals.inviteToCommunityTitle', { name: this.targetName() || this.targetUsername() }),
  );

  private myCommunities = signal<CommunityResponse[]>([]);
  loading = signal(false);
  private loaded = false;

  private states = signal<Record<string, InviteState>>({});

  activeCommunities = computed(() =>
    this.myCommunities().filter((c) => c.status !== 'ARCHIVED'),
  );

  constructor() {
    effect(() => {
      if (this.open() && !this.loaded) {
        this.loaded = true;
        this.loadCommunities();
      }
    });
  }

  stateOf(communityId: string): InviteState {
    return this.states()[communityId] ?? 'idle';
  }

  label(communityId: string): string {
    switch (this.stateOf(communityId)) {
      case 'sending':
        return this.i18n.t('common.loading');
      case 'sent':
        return this.i18n.t('modals.invited');
      case 'failed':
        return this.i18n.t('modals.invite');
      default:
        return this.i18n.t('modals.invite');
    }
  }

  invite(community: CommunityResponse): void {
    if (this.stateOf(community.id) === 'sending' || this.stateOf(community.id) === 'sent') return;
    this.setState(community.id, 'sending');

    const identifier = community.communitySlug || community.id;
    this.communityInviteService.inviteUser(identifier, this.targetUsername(), {}).subscribe({
      next: () => {
        this.setState(community.id, 'sent');
        this.toastService.success(`Convite para ${community.communityName} enviado.`);
      },
      error: (error) => {
        console.error('Failed to invite user to community:', error);
        this.setState(community.id, 'failed');
        this.toastService.error(error?.error?.message ?? 'Não foi possível enviar o convite.');
      },
    });
  }

  private setState(communityId: string, state: InviteState): void {
    this.states.update((current) => ({ ...current, [communityId]: state }));
  }

  private loadCommunities(): void {
    this.loading.set(true);

    this.communityService.listMyCommunities(0, COMMUNITIES_PAGE_SIZE).subscribe({
      next: (response) => {
        this.myCommunities.set(response.content);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch my communities:', error);
        this.loading.set(false);
        this.toastService.error('Não foi possível carregar suas comunidades.');
      },
    });
  }
}
