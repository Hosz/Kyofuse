import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { PrivacySettingsStore } from './privacy-settings-store';

@Component({
  selector: 'app-privacy-settings-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, AppSidebarComponent],
  templateUrl: './privacy-settings-shell.html',
  styleUrl: './privacy-settings-shell.css',
})
export class PrivacySettingsShellComponent {
  readonly store = inject(PrivacySettingsStore);

  readonly sections = [
    { path: 'perfil', label: 'Visibilidade do Perfil', icon: 'visibility' },
    { path: 'conteudo', label: 'Visibilidade de Conteúdo', icon: 'article' },
    { path: 'conexoes', label: 'Visibilidade de Conexões', icon: 'diversity_3' },
    { path: 'interacoes', label: 'Permissões de Interação', icon: 'shield_person' },
    { path: 'bloqueados', label: 'Usuários Bloqueados', icon: 'block' },
  ];

  ngOnInit(): void {
    this.store.load();
  }
}
