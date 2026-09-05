import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ChatDockComponent } from './components/chat/chat-dock/chat-dock';
import { ToastHostComponent } from './components/shared/toast-host/toast-host';
import { MobileHeaderComponent } from './components/layout/mobile-header/mobile-header';
import { MobileNavComponent } from './components/layout/mobile-nav/mobile-nav';
import { MobileDrawerComponent } from './components/layout/mobile-drawer/mobile-drawer';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    ChatDockComponent,
    ToastHostComponent,
    MobileHeaderComponent,
    MobileNavComponent,
    MobileDrawerComponent,
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('kyofuse-app');
}
