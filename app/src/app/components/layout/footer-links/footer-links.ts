import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../../core/services/ui/toast.service';

import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-footer-links',
  imports: [RouterLink, TranslatePipe],
  templateUrl: './footer-links.html',
  styleUrl: './footer-links.css',
})
export class FooterLinksComponent {
  private toastService = inject(ToastService);

  showPolicy(name: string): void {
    this.toastService.info(`Os ${name} da plataforma serão publicados em breve.`);
  }
}
 
