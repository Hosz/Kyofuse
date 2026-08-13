import { Component, input, output, signal } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { ProfileEntity } from '../../../shared/models/profile.model';

@Component({
  selector: 'app-invite-modal',
  imports: [ModalComponent],
  templateUrl: './invite-modal.html',
  styleUrl: './invite-modal.css',
})
export class InviteModalComponent {
  open = input(false);
  targetName = input.required<string>();
  teams = input<ProfileEntity[]>([]);
  communities = input<ProfileEntity[]>([]);

  closed = output<void>();

  private readonly sentIds = signal(new Set<string>());

  isSent(id: string): boolean {
    return this.sentIds().has(id);
  }

  toggleInvite(id: string): void {
    this.sentIds.update((ids) => {
      const next = new Set(ids);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }
}
