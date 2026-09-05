import { Routes } from '@angular/router';
import { PrivacySettingsStore } from './privacy-settings-store';

export const PRIVACY_SETTINGS_ROUTES: Routes = [
  {
    path: '',
    providers: [PrivacySettingsStore],
    loadComponent: () =>
      import('./privacy-settings-shell').then((m) => m.PrivacySettingsShellComponent),
    title: 'Kyofuse | Configurações',
    children: [
      { path: '', redirectTo: 'conta', pathMatch: 'full' },
      {
        path: 'conta',
        loadComponent: () =>
          import('./sections/account-settings/account-settings').then((m) => m.AccountSettingsSectionComponent),
      },
      {
        path: 'senha',
        loadComponent: () =>
          import('./sections/password-settings/password-settings').then((m) => m.PasswordSettingsSectionComponent),
      },
      {
        path: 'gerenciamento',
        loadComponent: () =>
          import('./sections/account-management/account-management').then((m) => m.AccountManagementSectionComponent),
      },
      {
        path: 'perfil',
        loadComponent: () =>
          import('./sections/profile-visibility/profile-visibility').then((m) => m.ProfileVisibilitySectionComponent),
      },
      { path: 'conteudo', redirectTo: 'perfil', pathMatch: 'full' },
      { path: 'conexoes', redirectTo: 'perfil', pathMatch: 'full' },
      {
        path: 'bloqueados',
        loadComponent: () =>
          import('./sections/blocked-users/blocked-users').then((m) => m.BlockedUsersSectionComponent),
      },
      {
        path: 'interacoes',
        loadComponent: () =>
          import('./sections/interaction-permissions/interaction-permissions').then(
            (m) => m.InteractionPermissionsSectionComponent,
          ),
      },
      {
        path: 'seguranca',
        loadComponent: () =>
          import('./sections/security-settings/security-settings').then(
            (m) => m.SecuritySettingsSectionComponent,
          ),
      },
      {
        path: 'preferencias',
        loadComponent: () =>
          import('./sections/preferences-settings/preferences-settings').then(
            (m) => m.PreferencesSettingsSectionComponent,
          ),
      },
    ],
  },
];
