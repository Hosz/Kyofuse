import { Component, inject, input } from '@angular/core';
import { NavItem } from '../../../shared/models/social.model';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NotificationService } from '../../../core/services/notifications/notification.service';

@Component({
  selector: 'app-sidebar-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar-nav.html',
  styleUrl: './sidebar-nav.css',
})
export class SidebarNavComponent {
  private notificationService = inject(NotificationService);

  readonly unreadNotificationsCount = this.notificationService.unreadCount;

  navItems = input<NavItem[]>([
    { icon: 'home', label: 'Página Inicial', route: '/home' },
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
  }
}
