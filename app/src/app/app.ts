import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ChatDockComponent } from './components/chat/chat-dock/chat-dock';
import { ToastHostComponent } from './components/shared/toast-host/toast-host';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ChatDockComponent, ToastHostComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('kyofuse-app');
}
