import { Component, input } from '@angular/core';
import { FeaturedTeam } from '../../../shared/models/social.model';

import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-featured-teams',
  imports: [RouterLink],
  templateUrl: './featured-teams.html',
  styleUrl: './featured-teams.css',
})
export class FeaturedTeamsComponent {
  teams = input<FeaturedTeam[]>([
    { name: 'Vanguard Elite', playersCount: '5/5 PLAYERS', status: 'PRO' },
    { name: 'Neon Shadows', playersCount: '4/5 PLAYERS', status: 'SEMI-PRO' },
  ]);
}