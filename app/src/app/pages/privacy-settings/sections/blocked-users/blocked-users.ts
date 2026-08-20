import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BlockService } from '../../../../core/services/block/block.service';
import { UserBlockResponse } from '../../../../models/block/block.model';
import { FALLBACK_AVATAR_URL } from '../../../../shared/utils/format.util';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-blocked-users-section',
  imports: [RouterLink],
  templateUrl: './blocked-users.html',
  styleUrl: './blocked-users.css',
})
export class BlockedUsersSectionComponent {
  private blockService = inject(BlockService);

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  blocked = signal<UserBlockResponse[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  page = signal(0);
  totalPages = signal(0);
  totalBlocked = signal(0);

  /** Id de quem está sendo desbloqueado agora, pra travar só aquele botão. */
  unblockingId = signal<string | null>(null);
  unblockError = signal<string | null>(null);

  ngOnInit(): void {
    this.load(0);
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) return;
    this.load(page);
  }

  unblock(entry: UserBlockResponse): void {
    if (this.unblockingId()) return;

    this.unblockingId.set(entry.blockedId);
    this.unblockError.set(null);

    this.blockService.unblockUser(entry.blockedId).subscribe({
      next: () => {
        this.unblockingId.set(null);
        this.blocked.update((list) => list.filter((item) => item.blockedId !== entry.blockedId));
        this.totalBlocked.update((total) => Math.max(0, total - 1));
      },
      error: (error) => {
        console.error('Failed to unblock user:', error);
        this.unblockingId.set(null);
        this.unblockError.set(error?.error?.message ?? 'Não foi possível desbloquear esse usuário.');
      },
    });
  }

  private load(page: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.blockService.getBlockedUsers(page, PAGE_SIZE).subscribe({
      next: (response) => {
        this.blocked.set(response.content);
        this.page.set(page);
        this.totalPages.set(response.totalPages);
        this.totalBlocked.set(response.totalElements);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch blocked users:', error);
        this.error.set('Não foi possível carregar a lista de bloqueados.');
        this.loading.set(false);
      },
    });
  }
}
