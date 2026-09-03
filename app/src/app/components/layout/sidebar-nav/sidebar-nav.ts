import { Component, inject, input } from '@angular/core';
import { NavItem } from '../../../shared/models/social.model';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NotificationService } from '../../../core/services/notifications/notification.service';
import { ConversationService } from '../../../core/services/chat/conversation.service';

@Component({
  selector: 'app-sidebar-nav',
  imports: [RouterLink, RouterLinkActive],
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
    { icon: 'home', label: 'Página Inicial', route: '/home' },
    { icon: 'explore', label: 'Explorar', route: '/explorar' },
    { icon: 'military_tech', label: 'Ranking', route: '/ranking' },
    { icon: 'notifications', label: 'Notificações', route: '/notificacoes' },
    { icon: 'emoji_events', label: 'Torneios', route: '/torneios' },
    { icon: 'chat_bubble', label: 'Conversas', route: '/chats' },
    { icon: 'person', label: 'Perfil', route: '/perfil' },
    { icon: 'groups', label: 'Times', route: '/times' },
    { icon: 'group', label: 'Comunidade', route: '/comunidade' },
    { icon: 'route', label: 'Roadmap', route: '/roadmap' },
    { icon: 'settings', label: 'Configurações', route: '/configuracoes' },
  ]);

  ngOnInit(): void {
    this.notificationService.refreshUnreadCount();
    this.conversationService.refreshUnreadStatus();
  }
}
