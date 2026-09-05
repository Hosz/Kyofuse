import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';
import { MobileDrawerService } from '../../../core/services/ui/mobile-drawer.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { NotificationService } from '../../../core/services/notifications/notification.service';
import { ConversationService } from '../../../core/services/chat/conversation.service';
import { CurrentUserService } from '../../../core/services/profile/current-user.service';
import { ProfileService } from '../../../core/services/profile/profile.service';
@Component({
  selector: 'app-mobile-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mobile-header.html',
})
export class MobileHeaderComponent {
  private readonly router = inject(Router);
  private readonly drawerService = inject(MobileDrawerService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly conversationService = inject(ConversationService);
  private readonly profileService = inject(ProfileService);

  readonly unreadNotificationsCount = this.notificationService.unreadCount;
  readonly unreadChatsCount = this.conversationService.unreadCount;
  readonly hasUnread = computed(() => this.unreadNotificationsCount() > 0 || this.unreadChatsCount() > 0);

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url }
  );

  readonly avatarUrl = computed(() => {
    // Loaded optionally
    return null;
  });

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

  openDrawer(): void {
    this.drawerService.open();
  }
}
