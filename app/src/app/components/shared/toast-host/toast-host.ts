import { Component, inject } from '@angular/core';
import { ToastService } from '../../../core/services/ui/toast.service';

@Component({
  selector: 'app-toast-host',
  imports: [],
  templateUrl: './toast-host.html',
  styleUrl: './toast-host.css',
})
export class ToastHostComponent {
  private toastService = inject(ToastService);

  toasts = this.toastService.toasts;

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }
}
