import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { PrivacySettingsStore } from './privacy-settings-store';
import { SkeletonComponent } from '../../components/shared/skeleton/skeleton';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

@Component({
  selector: 'app-privacy-settings-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, AppSidebarComponent, SkeletonComponent, TranslatePipe],
  templateUrl: './privacy-settings-shell.html',
  styleUrl: './privacy-settings-shell.css',
})
export class PrivacySettingsShellComponent {
  readonly store = inject(PrivacySettingsStore);

  readonly accountSections = [
    { path: 'conta', key: 'settings.accountData', icon: 'manage_accounts' },
    { path: 'senha', key: 'settings.passwordChange', icon: 'lock_reset' },
    { path: 'gerenciamento', key: 'settings.accountManagement', icon: 'dangerous' },
  ];

  readonly sections = [
    { path: 'perfil', key: 'settings.profileVisibility', icon: 'visibility' },
    { path: 'interacoes', key: 'settings.interactionPermissions', icon: 'shield_person' },
    { path: 'bloqueados', key: 'settings.blockedUsers', icon: 'block' },
  ];

  readonly securitySections = [
    { path: 'seguranca', key: 'settings.twoFactor', icon: 'security' },
  ];

  readonly preferenceSections = [
    { path: 'preferencias', key: 'settings.preferences', icon: 'palette' },
  ];

  get allSections() {
    return [...this.preferenceSections, ...this.accountSections, ...this.sections, ...this.securitySections];
  }

  ngOnInit(): void {
    this.store.load();
  }
}
