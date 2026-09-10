import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MobileDrawerService } from '../../../core/services/ui/mobile-drawer.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { AccountManagerService } from '../../../core/services/auth/account-manager.service';
import { NotificationService } from '../../../core/services/notifications/notification.service';
import { ConversationService } from '../../../core/services/chat/conversation.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { UserProfileCardComponent } from '../user-profile-card/user-profile-card';

@Component({
  selector: 'app-mobile-drawer',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    TranslatePipe,
    UserProfileCardComponent,
  ],
  templateUrl: './mobile-drawer.html',
})
export class MobileDrawerComponent {
  private readonly router = inject(Router);
  private readonly drawerService = inject(MobileDrawerService);
  private readonly authService = inject(AuthService);
  private readonly accountManager = inject(AccountManagerService);
  private readonly notificationService = inject(NotificationService);
  private readonly conversationService = inject(ConversationService);

  readonly isOpen = this.drawerService.isOpen;
  readonly unreadNotificationsCount = this.notificationService.unreadCount;
  readonly unreadChatsCount = this.conversationService.unreadCount;

  readonly navItems = [
    { icon: 'home', labelKey: 'nav.home', route: '/home' },
    { icon: 'explore', labelKey: 'nav.explore', route: '/explorar' },
    { icon: 'military_tech', labelKey: 'nav.leaderboard', route: '/ranking' },
    { icon: 'notifications', labelKey: 'nav.notifications', route: '/notificacoes' },
    { icon: 'emoji_events', labelKey: 'nav.tournaments', route: '/torneios' },
    { icon: 'chat_bubble', labelKey: 'nav.chats', route: '/chats' },
    { icon: 'person', labelKey: 'nav.profile', route: '/perfil' },
    { icon: 'groups', labelKey: 'nav.teams', route: '/times' },
    { icon: 'group', labelKey: 'nav.communities', route: '/comunidade' },
    { icon: 'route', labelKey: 'nav.roadmap', route: '/roadmap' },
    { icon: 'settings', labelKey: 'nav.settings', route: '/configuracoes' },
  ];

  close(): void {
    this.drawerService.close();
  }

  logout(): void {
    this.close();
    this.authService.logout().subscribe({
      next: () => this.handleLogoutRedirect(),
      error: () => {
        this.authService.clearSession();
        this.handleLogoutRedirect();
      },
    });
  }

  private handleLogoutRedirect(): void {
    const hasOtherAccounts = this.accountManager.savedAccounts().length > 0;
    if (hasOtherAccounts) {
      this.router.navigate(['/auth'], { queryParams: { accountsPrompt: 'true' } });
    } else {
      this.router.navigateByUrl('');
    }
  }
}
