import { Component, output } from '@angular/core';

@Component({
  selector: 'app-social-auth-buttons',
  imports: [],
  templateUrl: './social-auth-buttons.html',
  styleUrl: './social-auth-buttons.css',
})
export class SocialAuthButtonsComponent {
  googleClick = output<void>();
  steamClick = output<void>();
}
