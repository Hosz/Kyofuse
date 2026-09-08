import { Component, input } from '@angular/core';
import { ProfileRankStats } from '../../../shared/models/profile.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-live-stats-card',
  imports: [TranslatePipe],
  templateUrl: './live-stats-card.html',
  styleUrl: './live-stats-card.css',
})
export class LiveStatsCardComponent {
  stats = input.required<ProfileRankStats>();
}
