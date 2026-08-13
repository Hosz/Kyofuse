import { Component, input } from '@angular/core';
import { AuthFeature } from '../../../shared/models/auth.model';

@Component({
  selector: 'app-auth-hero',
  imports: [],
  templateUrl: './auth-hero.html',
  styleUrl: './auth-hero.css',
})
export class AuthHeroComponent {
  features = input<AuthFeature[]>([
    { icon: 'groups', label: 'Monte seu time' },
    { icon: 'military_tech', label: 'Global Elite Stats' },
    { icon: 'gps_fixed', label: 'Scrim Finder' },
  ]);
}
