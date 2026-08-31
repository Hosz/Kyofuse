import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { PrivacySettingsStore } from './privacy-settings-store';

import { SkeletonComponent } from '../../components/shared/skeleton/skeleton';

@Component({
  selector: 'app-privacy-settings-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, AppSidebarComponent, SkeletonComponent],
  templateUrl: './privacy-settings-shell.html',
  styleUrl: './privacy-settings-shell.css',
})
export class PrivacySettingsShellComponent {
  readonly store = inject(PrivacySettingsStore);

  readonly accountSections = [
    { path: 'conta', label: 'Dados da Conta', icon: 'manage_accounts' },
    { path: 'senha', label: 'Alteração de Senha', icon: 'lock_reset' },
  ];

  readonly sections = [
    { path: 'perfil', label: 'Visibilidade do Perfil', icon: 'visibility' },
    { path: 'conteudo', label: 'Visibilidade de Conteúdo', icon: 'article' },
    { path: 'conexoes', label: 'Visibilidade de Conexões', icon: 'diversity_3' },
    { path: 'interacoes', label: 'Permissões de Interação', icon: 'shield_person' },
    { path: 'bloqueados', label: 'Usuários Bloqueados', icon: 'block' },
  ];

  readonly securitySections = [
    { path: 'seguranca', label: 'Autenticação em Duas Etapas', icon: 'security' },
  ];

  ngOnInit(): void {
    this.store.load();
  }
}
