import { Component, input, output } from '@angular/core';
import { AuthTab, AuthTabId } from '../../../shared/models/auth.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-auth-tabs',
  imports: [TranslatePipe],
  templateUrl: './auth-tabs.html',
  styleUrl: './auth-tabs.css',
})
export class AuthTabsComponent {
  tabs = input<AuthTab[]>([
    { id: 'login', label: 'Entrar' },
    { id: 'register', label: 'Registrar' },
  ]);

  active = input<AuthTabId>('login');

  tabSelected = output<AuthTabId>();

  onSelect(tab: AuthTab): void {
    this.tabSelected.emit(tab.id);
  }
}
