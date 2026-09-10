import { Component, computed, inject, input, output } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService } from '../../../core/i18n/i18n.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { FullMessageViewData } from './full-message-modal.types';

@Component({
  selector: 'app-full-message-modal',
  standalone: true,
  imports: [ModalComponent, TranslatePipe],
  templateUrl: './full-message-modal.html',
  styleUrl: './full-message-modal.css',
})
export class FullMessageModalComponent {
  data = input<FullMessageViewData | null>(null);
  closed = output<void>();
  openImage = output<string>();

  private toastService = inject(ToastService);
  private i18n = inject(I18nService);

  readonly authorAvatar = computed(() => this.data()?.senderAvatarUrl || FALLBACK_AVATAR_URL);
  readonly formattedTime = computed(() => this.data()?.tooltipTime || this.data()?.timestamp || '');

  async copyMessage(text: string): Promise<void> {
    try {
      if (typeof navigator !== 'undefined' && navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(text);
      } else if (typeof document !== 'undefined') {
        const textarea = document.createElement('textarea');
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
      }
      this.toastService.success(this.i18n.t('chat.textCopied'));
    } catch {
      this.toastService.error('Erro ao copiar texto');
    }
  }
}
