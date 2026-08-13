import { Routes } from '@angular/router';
import { authGuard } from './core/guard/auth-guard';
import { guestGuard } from './core/guard/guest-guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./pages/auth/auth').then((m) => m.AuthComponent),
    title: 'Kyofuse | Acesso',
  },
  {
    path: 'home',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/home/home').then((m) => m.HomeComponent),
    title: 'Kyofuse | Página Inicial',
  },
  {
    path: 'notificacoes',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/notifications/notifications').then((m) => m.NotificationsComponent),
    title: 'Kyofuse | Notificações',
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/profile/profile').then((m) => m.ProfileComponent),
    title: 'Kyofuse | Perfil',
  },
  {
    path: 'perfil/editar',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/profile-edit/profile-edit').then((m) => m.ProfileEditComponent),
    title: 'Kyofuse | Editar perfil',
  },
  {
    path: 'perfil/:userId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/profile/profile').then((m) => m.ProfileComponent),
    title: 'Kyofuse | Perfil',
  },
  {
    path: 'configuracoes',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./pages/privacy-settings/privacy-settings.routes').then((m) => m.PRIVACY_SETTINGS_ROUTES),
  },
  {
    path: 'post/:postId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/post-detail/post-detail').then((m) => m.PostDetailComponent),
    title: 'Kyofuse | Publicação',
  },
  {
    path: 'busca',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/search-results/search-results').then((m) => m.SearchResultsComponent),
    title: 'Kyofuse | Busca',
  },
  {
    path: 'times',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/teams-hub/teams-hub').then((m) => m.TeamsHubComponent),
    title: 'Kyofuse | Times',
  },
  {
    path: 'times/:teamId/admin',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/team-admin/team-admin').then((m) => m.TeamAdminComponent),
    title: 'Kyofuse | Administração do Time',
  },
  {
    path: 'times/:teamId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/team/team').then((m) => m.TeamComponent),
    title: 'Kyofuse | Time',
  },
  {
    path: 'chats',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/chat/chat').then((m) => m.ChatComponent),
    title: 'Kyofuse | Conversas',
  },
  {
    path: 'chats/:chatId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/chat/chat').then((m) => m.ChatComponent),
    title: 'Kyofuse | Conversas',
  },
  {
    path: '**',
    redirectTo: '',
  },
];
