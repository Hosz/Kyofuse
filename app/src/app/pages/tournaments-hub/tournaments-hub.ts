import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { DiscoverySidebarComponent } from '../../components/discovery/discovery-sidebar/discovery-sidebar';
import { ToastService } from '../../core/services/ui/toast.service';

@Component({
  selector: 'app-tournaments-hub',
  imports: [RouterLink, AppSidebarComponent, DiscoverySidebarComponent],
  templateUrl: './tournaments-hub.html',
  styleUrl: './tournaments-hub.css',
})
export class TournamentsHubComponent {
  private toastService = inject(ToastService);

  notifyMe(): void {
    this.toastService.success('Perfeito! Você será notificado assim que a Central de Torneios estiver disponível.');
  }
}
