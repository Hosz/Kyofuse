import { Component, inject, input } from '@angular/core';
import { NavItem } from '../../../shared/models/social.model';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NotificationService } from '../../../core/services/notifications/notification.service';
import { ConversationService } from '../../../core/services/chat/conversation.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-sidebar-nav',
  imports: [RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './sidebar-nav.html',
  styleUrl: './sidebar-nav.css',
})
export class SidebarNavComponent {
  private notificationService = inject(NotificationService);
  private conversationService = inject(ConversationService);

  readonly unreadNotificationsCount = this.notificationService.unreadCount;
  readonly unreadChatsCount = this.conversationService.unreadCount;
  readonly hasUnreadChats = this.conversationService.hasUnread;

  navItems = input<NavItem[]>([
    { icon: 'home', label: 'Página Inicial', labelKey: 'nav.home', route: '/home' },
    { icon: 'explore', label: 'Explorar', labelKey: 'nav.explore', route: '/explorar' },
    { icon: 'military_tech', label: 'Ranking', labelKey: 'nav.leaderboard', route: '/ranking' },
    { icon: 'notifications', label: 'Notificações', labelKey: 'nav.notifications', route: '/notificacoes' },
    { icon: 'emoji_events', label: 'Torneios', labelKey: 'nav.tournaments', route: '/torneios' },
    { icon: 'chat_bubble', label: 'Conversas', labelKey: 'nav.chats', route: '/chats' },
    { icon: 'person', label: 'Perfil', labelKey: 'nav.profile', route: '/perfil' },
    { icon: 'groups', label: 'Times', labelKey: 'nav.teams', route: '/times' },
    { icon: 'group', label: 'Comunidade', labelKey: 'nav.communities', route: '/comunidade' },
    { icon: 'route', label: 'Roadmap', labelKey: 'nav.roadmap', route: '/roadmap' },
    { icon: 'settings', label: 'Configurações', labelKey: 'nav.settings', route: '/configuracoes' },
  ]);

  ngOnInit(): void {
    this.notificationService.refreshUnreadCount(true);
    this.conversationService.refreshUnreadStatus();
  }
}
