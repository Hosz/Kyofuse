import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';
import { AuthService } from '../../../core/services/auth/auth.service';
import { NotificationService } from '../../../core/services/notifications/notification.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-mobile-nav',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './mobile-nav.html',
})
export class MobileNavComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);

  readonly unreadNotificationsCount = this.notificationService.unreadCount;

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url }
  );

  readonly isVisible = computed(() => {
    const url = this.currentUrl();
    const isAuth =
      url === '' ||
      url === '/' ||
      url === '/auth' ||
      url.startsWith('/auth/') ||
      url.startsWith('/recuperar-senha') ||
      url.startsWith('/verificar-email') ||
      url.startsWith('/setup');

    return !isAuth && this.authService.isAuthenticated();
  });
}
