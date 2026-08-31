import { Routes } from '@angular/router';
import { authGuard } from './core/guard/auth-guard';
import { guestGuard } from './core/guard/guest-guard';
import { profileSetupGuard } from './core/guard/profile-setup-guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./pages/auth/auth').then((m) => m.AuthComponent),
    title: 'Kyofuse | Acesso',
  },
  {
    path: 'recuperar-senha',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./pages/reset-password/reset-password').then((m) => m.ResetPasswordComponent),
    title: 'Kyofuse | Redefinir Senha',
  },
  {
    path: 'verificar-email',
    loadComponent: () =>
      import('./pages/verify-email/verify-email').then((m) => m.VerifyEmailComponent),
    title: 'Kyofuse | Confirmar E-mail',
  },
  {
    path: 'auth/steam/callback',
    loadComponent: () =>
      import('./pages/steam-callback/steam-callback').then((m) => m.SteamCallbackComponent),
    title: 'Kyofuse | Autenticando com Steam',
  },
  {
    path: 'setup',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/profile-setup/profile-setup').then((m) => m.ProfileSetupComponent),
    title: 'Kyofuse | Configurar Perfil',
  },
  {
    path: 'home',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/home/home').then((m) => m.HomeComponent),
    title: 'Kyofuse | Página Inicial',
  },
  {
    path: 'notificacoes',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/notifications/notifications').then((m) => m.NotificationsComponent),
    title: 'Kyofuse | Notificações',
  },
  {
    path: 'torneios',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/tournaments-hub/tournaments-hub').then((m) => m.TournamentsHubComponent),
    title: 'Kyofuse | Torneios & Campeonatos',
  },
  {
    path: 'roadmap',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/roadmap/roadmap').then((m) => m.RoadmapComponent),
    title: 'Kyofuse | Roadmap de Desenvolvimento',
  },
  {
    path: 'explore',
    redirectTo: 'roadmap',
  },
  {
    path: 'perfil',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/profile/profile').then((m) => m.ProfileComponent),
    title: 'Kyofuse | Perfil',
  },
  {
    path: 'perfil/editar',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/profile-edit/profile-edit').then((m) => m.ProfileEditComponent),
    title: 'Kyofuse | Editar perfil',
  },
  {
    path: 'perfil/:username',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/profile/profile').then((m) => m.ProfileComponent),
    title: 'Kyofuse | Perfil',
  },
  {
    path: 'configuracoes',
    canActivate: [authGuard, profileSetupGuard],
    loadChildren: () =>
      import('./pages/privacy-settings/privacy-settings.routes').then((m) => m.PRIVACY_SETTINGS_ROUTES),
  },
  {
    path: 'post/:postId',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/post-detail/post-detail').then((m) => m.PostDetailComponent),
    title: 'Kyofuse | Publicação',
  },
  {
    path: 'busca',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/search-results/search-results').then((m) => m.SearchResultsComponent),
    title: 'Kyofuse | Busca',
  },
  {
    path: 'times',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/teams-hub/teams-hub').then((m) => m.TeamsHubComponent),
    title: 'Kyofuse | Times',
  },
  {
    path: 'times/:teamId/admin',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/team-admin/team-admin').then((m) => m.TeamAdminComponent),
    title: 'Kyofuse | Administração do Time',
  },
  {
    path: 'times/:teamId',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/team/team').then((m) => m.TeamComponent),
    title: 'Kyofuse | Time',
  },
  {
    path: 'comunidade',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/communities-hub/communities-hub').then((m) => m.CommunitiesHubComponent),
    title: 'Kyofuse | Comunidades',
  },
  {
    path: 'comunidade/:communityId/membros',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/community-members/community-members').then((m) => m.CommunityMembersComponent),
    title: 'Kyofuse | Membros da Comunidade',
  },
  {
    path: 'comunidade/:communityId',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/community/community').then((m) => m.CommunityComponent),
    title: 'Kyofuse | Comunidade',
  },
  {
    path: 'chats',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/chat/chat').then((m) => m.ChatComponent),
    title: 'Kyofuse | Conversas',
  },
  {
    path: 'chats/:chatId',
    canActivate: [authGuard, profileSetupGuard],
    loadComponent: () =>
      import('./pages/chat/chat').then((m) => m.ChatComponent),
    title: 'Kyofuse | Conversas',
  },
  {
    path: '**',
    redirectTo: '',
  },
];
