import { Component, computed, input, output } from '@angular/core';
import { AppNotification } from '../../../shared/models/notification.model';

@Component({
  selector: 'app-notification-card',
  imports: [],
  templateUrl: './notification-card.html',
  styleUrl: './notification-card.css',
})
export class NotificationCardComponent {
  notification = input.required<AppNotification>();
  /** Itens que não são notificações de verdade (ex.: pedido de amizade) não têm o que ler
   * ou arquivar — só as ações de aceitar/recusar fazem sentido neles. */
  showStatusActions = input(true);

  toggleRead = output<void>();
  toggleArchive = output<void>();
  accept = output<void>();
  decline = output<void>();

  isUnread = computed(() => this.notification().status === 'unread');
  isArchived = computed(() => this.notification().status === 'archived');

  cardClass = computed(() => {
    const base = this.isUnread() ? 'bg-surface-container' : 'bg-surface-container-low';
    const accent = this.isUnread() ? 'border-l-primary-container' : 'border-l-transparent';
    const dim = this.isArchived() ? 'opacity-70' : '';
    return `${base} border border-outline-variant border-l-4 ${accent} ${dim}`;
  });
}
