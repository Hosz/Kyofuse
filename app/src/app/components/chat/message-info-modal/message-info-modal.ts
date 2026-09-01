import { Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../shared/modal/modal';
import { MessageInfoResponse } from '../../../models/chat/chat.model';
import { FALLBACK_AVATAR_URL, formatFullPostDateTime } from '../../../shared/utils/format.util';

@Component({
  selector: 'app-message-info-modal',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  templateUrl: './message-info-modal.html',
  styleUrl: './message-info-modal.css',
})
export class MessageInfoModalComponent {
  open = input(false);
  info = input<MessageInfoResponse | null>(null);
  loading = input(false);

  closed = output<void>();

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  readList = computed(() => {
    return this.info()?.receipts.filter((r) => r.readAt !== null) ?? [];
  });

  deliveredList = computed(() => {
    return this.info()?.receipts.filter((r) => r.deliveredAt !== null && r.readAt === null) ?? [];
  });

  formatDate(isoDate: string | null): string {
    return isoDate ? formatFullPostDateTime(isoDate) : '';
  }
}
