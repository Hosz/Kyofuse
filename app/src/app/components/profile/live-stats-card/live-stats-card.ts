import { Component, input } from '@angular/core';
import { ProfileRankStats } from '../../../shared/models/profile.model';

@Component({
  selector: 'app-live-stats-card',
  imports: [],
  templateUrl: './live-stats-card.html',
  styleUrl: './live-stats-card.css',
})
export class LiveStatsCardComponent {
  stats = input.required<ProfileRankStats>();
}
