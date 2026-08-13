import { Component, input } from '@angular/core';

@Component({
  selector: 'app-auth-field',
  imports: [],
  templateUrl: './auth-field.html',
  styleUrl: './auth-field.css',
})
export class AuthFieldComponent {
  label = input.required<string>();
  icon = input.required<string>();
  hasError = input(false);
  errorMessage = input<string | null>(null);
}
