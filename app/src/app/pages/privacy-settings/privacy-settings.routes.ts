import { Routes } from '@angular/router';
import { PrivacySettingsStore } from './privacy-settings-store';

export const PRIVACY_SETTINGS_ROUTES: Routes = [
  {
    path: '',
    providers: [PrivacySettingsStore],
    loadComponent: () =>
      import('./privacy-settings-shell').then((m) => m.PrivacySettingsShellComponent),
    title: 'Kyofuse | Privacidade',
    children: [
      { path: '', redirectTo: 'perfil', pathMatch: 'full' },
      {
        path: 'perfil',
        loadComponent: () =>
          import('./sections/profile-visibility/profile-visibility').then((m) => m.ProfileVisibilitySectionComponent),
      },
      {
        path: 'conteudo',
        loadComponent: () =>
          import('./sections/content-visibility/content-visibility').then((m) => m.ContentVisibilitySectionComponent),
      },
      {
        path: 'conexoes',
        loadComponent: () =>
          import('./sections/connections-visibility/connections-visibility').then(
            (m) => m.ConnectionsVisibilitySectionComponent,
          ),
      },
      {
        path: 'interacoes',
        loadComponent: () =>
          import('./sections/interaction-permissions/interaction-permissions').then(
            (m) => m.InteractionPermissionsSectionComponent,
          ),
      },
    ],
  },
];
