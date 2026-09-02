import { Component, inject, input } from '@angular/core';
import { ProfileHighlight } from '../../../shared/models/profile.model';
import { ToastService } from '../../../core/services/ui/toast.service';

@Component({
  selector: 'app-profile-highlights',
  imports: [],
  templateUrl: './profile-highlights.html',
  styleUrl: './profile-highlights.css',
})
export class ProfileHighlightsComponent {
  private toastService = inject(ToastService);
  highlights = input<ProfileHighlight[]>([]);

  openHighlight(highlight: ProfileHighlight): void {
    this.toastService.info(`Os clipes do destaque "${highlight.label}" estarão disponíveis em breve!`);
  }
}
